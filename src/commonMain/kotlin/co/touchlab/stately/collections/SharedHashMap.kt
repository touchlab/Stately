package co.touchlab.stately.collections

import co.touchlab.stately.concurrency.AtomicInt
import co.touchlab.stately.concurrency.AtomicReference
import co.touchlab.stately.concurrency.Lock
import co.touchlab.stately.concurrency.QuickLock
import co.touchlab.stately.freeze

/**
 * Mutithreaded hashmap which uses atomic structures internally to allow sharing across threads on all variants of Kotlin.
 * The implementation prefers consistency over performance and will lock its internal mutex on most operations. That
 * means it works but performance won't be great compared to the single threaded hash map.
 */
class SharedHashMap<K, V>(initialCapacity:Int = 16, val loadFactor:Float = 0.75.toFloat()):MutableMap<K, V>{

    internal data class Entry<K, V>(private val k:K, private val v:V):MutableMap.MutableEntry<K, V> {
        override val key: K
            get() = k
        override val value: V
            get() = v

        override fun setValue(newValue: V): V {
            throw UnsupportedOperationException()
        }
    }

    private var lock: Lock = QuickLock()
    private var threshold:AtomicInt
    private val atomSize = AtomicInt(0)
    private val buckets:AtomicReference<Array<AtomicReference<SharedLinkedList<Entry<K, V>>>>>

    internal inline fun <T> withLock(proc: () -> T): T {
        lock.lock()
        try {
            return proc.invoke()
        } finally {
            lock.unlock()
        }
    }

    init {
        var capacity = 1
        while (capacity < initialCapacity)
            capacity = capacity shl 1

        threshold = AtomicInt((capacity.toFloat() * loadFactor).toInt())
        buckets = AtomicReference(makeBuckets(capacity))

        freeze()
    }

    private fun makeBuckets(capacity: Int): Array<AtomicReference<SharedLinkedList<Entry<K, V>>>> {
        return (Array(capacity) {
            AtomicReference(SharedLinkedList<Entry<K, V>>().freeze())
        }).freeze()
    }

    private inline fun iterInternal(proc:(Entry<K, V>)->Unit){
        buckets.value.forEach {
            it.value.forEach {
                proc(it)
            }
        }
    }

    /**
     * Returns a set of all entries. This set is *not* thread safe and may only be used by the caller thread. However,
     * it is stable and will not change when the source map changes.
     */
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = withLock {
            val resultSet = HashSet<MutableMap.MutableEntry<K, V>>(atomSize.value)
            iterInternal { resultSet.add(it) }
            return NotReallyMutableSet(resultSet)
        }

    /**
     * Returns a set of all keys. This set is *not* thread safe and may only be used by the caller thread. However,
     * it is stable and will not change when the source map changes.
     */
    override val keys: MutableSet<K>
        get() = withLock {
            val keySet = HashSet<K>(atomSize.value)
            iterInternal { keySet.add(it.key) }
            return NotReallyMutableSet(keySet)
        }

    /**
     * Returns a collection of all values. This collection is *not* thread safe and may only be used by the caller thread. However,
     * it is stable and will not change when the source map changes.
     */
    override val values: MutableCollection<V>
        get() = withLock {
            val result = ArrayList<V>(atomSize.value)
            iterInternal { result.add(it.value) }
            return NotReallyMutableSet(result)
        }

    private class NotReallyMutableSet<T>(private val delegate:MutableCollection<T>):MutableSet<T>{
        override fun add(element: T): Boolean {
            throw UnsupportedOperationException()
        }

        override fun addAll(elements: Collection<T>): Boolean {
            throw UnsupportedOperationException()
        }

        override fun clear() {
            throw UnsupportedOperationException()
        }

        override fun iterator(): MutableIterator<T> = delegate.iterator()

        override fun remove(element: T): Boolean {
            throw UnsupportedOperationException()
        }

        override fun removeAll(elements: Collection<T>): Boolean {
            throw UnsupportedOperationException()
        }

        override fun retainAll(elements: Collection<T>): Boolean {
            throw UnsupportedOperationException()
        }

        override val size: Int
            get() = delegate.size

        override fun contains(element: T): Boolean = delegate.contains(element)

        override fun containsAll(elements: Collection<T>): Boolean = delegate.containsAll(elements)

        override fun isEmpty(): Boolean = delegate.isEmpty()
    }

    /**
     * Removes all values
     */
    override fun clear() = withLock {
        buckets.value.forEach {
            it.value.clear()
        }

        atomSize.value = 0
    }

    override fun containsKey(key: K): Boolean = get(key) != null

    override fun containsValue(value: V): Boolean = withLock {
        iterInternal {
            if(it.value == value)
                return@containsValue true
        }
        return false
    }

    override fun get(key: K): V? = withLock {
        val entryList = findEntryList(buckets.value, key)
        entryList.forEach {
            if(it.key == key)
                return@get it.value
        }

        return null
    }

    override fun isEmpty(): Boolean = atomSize.value == 0

    override fun put(key: K, value: V): V? = withLock {
        internalPut(key, value)
    }

    override fun putAll(from: Map<out K, V>) = withLock {
        from.entries.forEach { internalPut(it.key, it.value) }
    }

    override fun remove(key: K): V? = withLock {
        val entryList = findEntryList(buckets.value, key)
        var result : V? = null
        entryList.nodeIterator().forEach {
            if(it.nodeValue.key == key){
                result = it.nodeValue.value
                it.remove()
                atomSize.decrement()
                return@forEach
            }
        }

        return result
    }

    override val size: Int
        get() = atomSize.value

    private fun internalPut(key: K, value: V): V? {
        val entryList = findEntryList(buckets.value, key)
        var result: V? = null
        entryList.nodeIterator().forEach {
            if (it.nodeValue.key == key) {
                result = it.nodeValue.value
                it.remove()
                atomSize.decrement()
                return@forEach
            }
        }

        entryList.add(Entry(key, value).freeze())
        atomSize.increment()
        if (atomSize.value > threshold.value)
            resize(2 * buckets.value.size)

        return result
    }

    private fun resize(newCapacity:Int){
        val oldTable = buckets.value
        val newTable = makeBuckets(newCapacity)
        transfer(newTable, oldTable)
        buckets.value = newTable
        threshold.value = (newCapacity.toFloat() * loadFactor).toInt()
    }

    private fun transfer(newTable: Array<AtomicReference<SharedLinkedList<Entry<K, V>>>>, oldTable: Array<AtomicReference<SharedLinkedList<Entry<K, V>>>>) {
        oldTable.forEach {
            it.value.iterator().forEach {
                findEntryList(newTable, it.key).add(it)
            }
        }
    }

    internal fun currentBucketSize():Int = buckets.value.size

    private fun indexFor(h: Int, length: Int): Int {
        return h and length - 1
    }

    internal fun rehash(initHash: Int): Int {
        var h = initHash
        // This function ensures that hashCodes that differ only by
        // constant multiples at each bit position have a bounded
        // number of collisions (approximately 8 at default load factor).
        h = h xor (h.ushr(20) xor h.ushr(12))
        return h xor h.ushr(7) xor h.ushr(4)
    }

    private fun findEntryList(bucketArray: Array<AtomicReference<SharedLinkedList<Entry<K, V>>>>, key: K): SharedLinkedList<Entry<K, V>> {
        val hash = rehash(key.hashCode())
        val entryList = bucketArray.get(indexFor(hash, bucketArray.size)).value
        return entryList
    }

}