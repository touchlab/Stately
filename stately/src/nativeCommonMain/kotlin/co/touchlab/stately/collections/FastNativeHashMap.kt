package co.touchlab.stately.collections

import kotlin.native.concurrent.freeze

class FastNativeHashSet<T>() : MutableSet<T>{
    private val map = FastNativeHashMap<T, Unit>()

    override fun add(element: T): Boolean = map.put(element, Unit) == null

    override fun addAll(elements: Collection<T>): Boolean {
        return elements.map {
            add(it)
        }.any { it }
    }

    override fun clear() {
        map.clear()
    }

    override fun iterator(): MutableIterator<T> {
        return map.keys.iterator()
    }

    override fun remove(element: T): Boolean = map.remove(element) != null

    override fun removeAll(elements: Collection<T>): Boolean = elements.map { remove(it) }.any { it }

    override fun retainAll(elements: Collection<T>): Boolean = map.keys.retainAll(elements)

    override val size: Int
        get() = map.size

    override fun contains(element: T): Boolean = map.containsKey(element)

    override fun containsAll(elements: Collection<T>): Boolean = elements.map { contains(it) }.all { it }

    override fun isEmpty(): Boolean = map.isEmpty()
}

class FastNativeHashMap<K, V>() : MutableMap<K, V> {
    private val nativePtr = nativeMapCreate()

    override val size: Int
        get() = nativeMapSize(nativePtr)

    override fun containsKey(key: K): Boolean = nativeMapContains(nativePtr, key)

    override fun containsValue(value: V): Boolean = values.contains(value)

    override fun get(key: K): V? = nativeMapGet(nativePtr, key) as V?

    override fun isEmpty(): Boolean = size == 0

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = EntrySet()

    override val keys: MutableSet<K>
        get() = KeySet()

    override val values: MutableCollection<V>
        get() = ValueCollection()

    override fun clear() {
        nativeMapClear(nativePtr)
    }

    override fun put(key: K, value: V): V? = nativeMapPut(nativePtr, key.freeze(), value.freeze()) as V?

    override fun putAll(from: Map<out K, V>) {
        println("*** putAll size(${from.size}) ***")
        from.entries.forEach {
            println("key: ${it.key}/value: ${it.value}")
        }
        from.entries.forEach {
            put(it.key, it.value)
        }
    }

    override fun remove(key: K): V? = nativeMapRemove(nativePtr, key) as V?

    internal inner class ValueCollection: MutableCollection<V>{
        override val size: Int
            get() = this@FastNativeHashMap.size

        override fun contains(element: V): Boolean = this.any { it == element }

        override fun containsAll(elements: Collection<V>): Boolean = elements.all { contains(it) }

        override fun isEmpty(): Boolean = this@FastNativeHashMap.isEmpty()

        override fun add(element: V): Boolean {
            throw UnsupportedOperationException()
        }

        override fun addAll(elements: Collection<V>): Boolean {
            throw UnsupportedOperationException()
        }

        override fun clear() {
            this@FastNativeHashMap.clear()
        }

        override fun iterator(): MutableIterator<V> = Iterator(nativeMapBeginIter(nativePtr) as NativeMemory)

        override fun remove(element: V): Boolean {
            val iterator = iterator()
            iterator.forEach {
                if(it == element){
                    iterator.remove()
                    return true
                }
            }

            return false
        }

        override fun removeAll(elements: Collection<V>): Boolean = elements.map { remove(it) }.any { it }

        override fun retainAll(elements: Collection<V>): Boolean {
            val mapIter = this@FastNativeHashMap.iterator()
            var anyRemoved = false
            mapIter.forEach {
                if(!elements.contains(it.value))
                {
                    mapIter.remove()
                    anyRemoved = true
                }
            }
            return anyRemoved
        }

        internal inner class Iterator(private val nativeMemory: NativeMemory) : MutableIterator<V> {
            override fun hasNext(): Boolean = nativeMapIterHasNext(nativeMemory)

            override fun next(): V = nativeMapIterNextValue(nativeMemory) as V

            override fun remove() {
                nativeMapIterRemove(nativeMemory)
            }
        }
    }

    internal inner class KeySet : MutableSet<K> {
        override fun add(element: K): Boolean {
            throw UnsupportedOperationException()
        }

        override fun addAll(elements: Collection<K>): Boolean {
            throw UnsupportedOperationException()
        }

        override fun clear() {
            this@FastNativeHashMap.clear()
        }

        override fun iterator(): MutableIterator<K> = Iterator(nativeMapBeginIter(nativePtr) as NativeMemory)

        override fun remove(element: K): Boolean = this@FastNativeHashMap.remove(element) != null

        override fun removeAll(elements: Collection<K>): Boolean = elements.map { this@FastNativeHashMap.remove(it) != null }.any { it }

        override fun retainAll(elements: Collection<K>): Boolean {
            val myCopy = HashSet(this)
            myCopy.removeAll(elements)
            return removeAll(myCopy)
        }

        override val size: Int
            get() = this@FastNativeHashMap.size

        override fun contains(element: K): Boolean = this@FastNativeHashMap.containsKey(element)

        override fun containsAll(elements: Collection<K>): Boolean = elements.all { contains(it) }

        override fun isEmpty(): Boolean = this@FastNativeHashMap.isEmpty()

        internal inner class Iterator(private val nativeMemory: NativeMemory) : MutableIterator<K> {
            override fun hasNext(): Boolean = nativeMapIterHasNext(nativeMemory)

            override fun next(): K = nativeMapIterNextKey(nativeMemory) as K

            override fun remove() {
                nativeMapIterRemove(nativeMemory)
            }
        }
    }

    internal inner class EntrySet : MutableSet<MutableMap.MutableEntry<K, V>> {
        override fun add(element: MutableMap.MutableEntry<K, V>): Boolean {
            this@FastNativeHashMap[element.key] = element.value
            return true
        }

        override fun addAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean {
            elements.forEach {
                add(it)
            }

            return true
        }

        override fun clear() {
            this@FastNativeHashMap.clear()
        }

        override fun iterator(): MutableIterator<MutableMap.MutableEntry<K, V>> = Iterator(nativeMapBeginIter(nativePtr) as NativeMemory)

        override fun remove(element: MutableMap.MutableEntry<K, V>): Boolean = this@FastNativeHashMap.remove(element.key) != null

        override fun removeAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean = elements.map {
            this@FastNativeHashMap.remove(it.key) != null
        }.any { it }

        override fun retainAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean {
            val myCopy = HashSet(this)
            myCopy.removeAll(elements)
            return removeAll(myCopy)
        }

        override val size: Int
            get() = this@FastNativeHashMap.size

        override fun contains(element: MutableMap.MutableEntry<K, V>): Boolean = this@FastNativeHashMap.containsKey(element.key)

        override fun containsAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean = elements.all {
            contains(it)
        }

        override fun isEmpty(): Boolean = this@FastNativeHashMap.isEmpty()

        internal inner class Iterator(private val nativeMemory: NativeMemory) : MutableIterator<MutableMap.MutableEntry<K, V>> {
            override fun hasNext(): Boolean = nativeMapIterHasNext(nativeMemory)

            override fun next(): MutableMap.MutableEntry<K, V> {
                val key = nativeMapIterNextKey(nativeMemory) as K
                val value = nativeMapIterNextValue(nativeMemory) as V

                return IterEntry(key, value)
            }

            override fun remove() {
                nativeMapIterRemove(nativeMemory)
            }
        }

        internal inner class IterEntry<K, V>(override val key: K, override val value: V): MutableMap.MutableEntry<K, V> {
            override fun setValue(newValue: V): V {
                throw UnsupportedOperationException("Read only")
            }
        }
    }

    fun setTempVar(a: K){
        println("setTempVar before")
        nativeSetTempVar(nativePtr, a.freeze())
        println("setTempVar after")
    }

    fun grabTempVar():K{
        return nativeGrabTempVar(nativePtr) as K
    }

    fun refCount(a: Any?): Int{
        return nativeRefCount(a)
    }

    fun tempRefCount(): Int{
        return nativeTempRefCount(nativePtr)
    }
}

@SymbolName("Stately_map_create")
private external fun nativeMapCreate(): Long

@SymbolName("Stately_map_clear")
private external fun nativeMapClear(mapPtr: Long)

@SymbolName("Stately_map_destroy")
private external fun nativeMapDestroy(mapPtr: Long)

@SymbolName("Stately_map_size")
private external fun nativeMapSize(mapPtr: Long): Int

@SymbolName("Stately_map_contains")
private external fun nativeMapContains(mapPtr: Long, key: Any?): Boolean

@SymbolName("Stately_map_put")
private external fun nativeMapPut(mapPtr: Long, key: Any?, value: Any?): Any?

@SymbolName("Stately_map_remove")
private external fun nativeMapRemove(mapPtr: Long, key: Any?): Any?

@SymbolName("Stately_map_get")
private external fun nativeMapGet(mapPtr: Long, key: Any?): Any?

@SymbolName("Stately_map_beginIter")
private external fun nativeMapBeginIter(mapPtr: Long): Any?

@SymbolName("Stately_map_iterHasNext")
private external fun nativeMapIterHasNext(nativeMemory: NativeMemory): Boolean

@SymbolName("Stately_map_iterNextKey")
private external fun nativeMapIterNextKey(nativeMemory: NativeMemory): Any?

@SymbolName("Stately_map_iterNextValue")
private external fun nativeMapIterNextValue(nativeMemory: NativeMemory): Any?

@SymbolName("Stately_map_iterRemove")
private external fun nativeMapIterRemove(nativeMemory: NativeMemory)

@SymbolName("Stately_map_setTempVar")
private external fun nativeSetTempVar(mapPtr: Long, a: Any?)

@SymbolName("Stately_map_grabTempVar")
private external fun nativeGrabTempVar(mapPtr: Long): Any?
@SymbolName("Stately_map_tempRefCount")
private external fun nativeTempRefCount(mapPtr: Long): Int

@SymbolName("Stately_debug_refCount")
private external fun nativeRefCount(a: Any?): Int


/*
KRef Stately_map_setTempVar(KLong mapPtr, KRef a) {
    ((FastHashMap *) mapPtr)->setTempVar(a);
}

KRef Stately_map_grabTempVar(KLong mapPtr) {
    return ((FastHashMap *) mapPtr)->grabTempVar();
}

KInt Stately_debug_refCount(KRef a){
    return a->container()->refCount();
}*/