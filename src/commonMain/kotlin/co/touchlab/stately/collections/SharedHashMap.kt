package co.touchlab.stately.collections

import co.touchlab.stately.concurrency.AtomicInt
import co.touchlab.stately.concurrency.AtomicReference

class SharedHashMap<K, V>(initialCapacity:Int = 16, val loadFactor:Float = 0.75.toFloat()):MutableMap<K, V>{


    data class Entry<K, V>(private val k:K, private val v:V):MutableMap.MutableEntry<K, V> {
        override val key: K
            get() = k
        override val value: V
            get() = v

        override fun setValue(newValue: V): V {
            throw UnsupportedOperationException()
        }
    }

    var threshold:AtomicInt
    val atomSize = AtomicInt(0)
    val buckets:AtomicReference<Array<AtomicReference<SharedLinkedList<Entry<K, V>>>>>

    init {
        var capacity = 1
        while (capacity < initialCapacity)
            capacity = capacity shl 1

        threshold = AtomicInt((capacity.toFloat() * loadFactor).toInt())
        buckets = AtomicReference(makeBuckets(capacity))
    }

    private fun makeBuckets(capacity: Int): Array<AtomicReference<SharedLinkedList<Entry<K, V>>>> {
        return (Array(capacity) {
            AtomicReference(SharedLinkedList<Entry<K, V>>().mpfreeze())
        }).mpfreeze()
    }

    private inline fun iterInternal(proc:(Entry<K, V>)->Unit){
        buckets.value.forEach {
            it.value.forEach {
                proc(it)
            }
        }
    }

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() {
            val resultSet = HashSet<MutableMap.MutableEntry<K, V>>(atomSize.value)
            iterInternal { resultSet.add(it) }
            return resultSet
        }

    override val keys: MutableSet<K>
        get() {
            val keySet = HashSet<K>(atomSize.value)
            iterInternal { keySet.add(it.key) }
            return keySet
        }

    override val values: MutableCollection<V>
        get() {
            var result = ArrayList<V>(atomSize.value)
            iterInternal { result.add(it.value) }
            return result
        }

    override fun clear() {
        buckets.value.forEach {
            it.value.clear()
        }

        atomSize.value = 0
    }

    override fun containsKey(key: K): Boolean = get(key) != null

    override fun containsValue(value: V): Boolean {
        iterInternal {
            if(it.value == value)
                return@containsValue true
        }
        return false
    }

    override fun get(key: K): V? {
        val entryList = findEntryList(buckets.value, key)
        entryList.forEach {
            if(it.key == key)
                return@get it.value
        }

        return null
    }

    private fun findEntryList(bucketArray: Array<AtomicReference<SharedLinkedList<Entry<K, V>>>>, key: K): SharedLinkedList<Entry<K, V>> {
        val hash = rehash(key.hashCode())
        val entryList = bucketArray.get(indexFor(hash, bucketArray.size)).value
        return entryList
    }

    override fun isEmpty(): Boolean = atomSize.value == 0

    override fun put(key: K, value: V): V? {
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

        entryList.add(Entry(key, value).mpfreeze())
        atomSize.increment()
        if(atomSize.value > threshold.value)
            resize(2 * buckets.value.size)

        return result
    }

    override fun putAll(from: Map<out K, V>) {
        from.entries.forEach { put(it.key, it.value) }
    }

    override fun remove(key: K): V? {
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

    fun resize(newCapacity:Int){
        val oldTable = buckets.value
        val newTable = makeBuckets(newCapacity)
        transfer(newTable, oldTable)
        buckets.value = newTable
        threshold.value = (newCapacity.toFloat() * loadFactor).toInt()

        println("New capacity: ${newCapacity}")
        println("New threshold: ${threshold.value}")
    }

    fun transfer(newTable: Array<AtomicReference<SharedLinkedList<Entry<K, V>>>>, oldTable: Array<AtomicReference<SharedLinkedList<Entry<K, V>>>>) {
        val newCapacity = newTable.size
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

    fun rehash(initHash: Int): Int {
        var h = initHash
        // This function ensures that hashCodes that differ only by
        // constant multiples at each bit position have a bounded
        // number of collisions (approximately 8 at default load factor).
        h = h xor (h.ushr(20) xor h.ushr(12))
        return h xor h.ushr(7) xor h.ushr(4)
    }
}