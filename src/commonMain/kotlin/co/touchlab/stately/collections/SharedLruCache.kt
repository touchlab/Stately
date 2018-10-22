package co.touchlab.stately.collections

import co.touchlab.stately.concurrency.Lock
import co.touchlab.stately.concurrency.QuickLock

/**
 * Implementation of a least recently used cache, multithreading aware for kotlin multiplatform.
 *
 * You provide a maximum number of cached entries, and (optionally) a lambda to call when something is removed.
 *
 * Operations on this collection aggressively lock, to err on the side of caution and sanity. Bear this in mind
 * if using in a high volume context.
 *
 * Each key will only retain a single value. When an entry is removed due to exceeding capacity, onRemove is called.
 * However, if adding an entry replaces an existing entry, the old entry will be returned, and onRemove WILL NOT be called.
 * If resource management depends on onRemove, keep this in mind. You'll need to handle a duplicate/replacement add.
 *
 * Along those lines, if you are managing resources with onRemove, keep in mind that if you abandon the cache and memory
 * is reclaimed, onRemove WILL NOT be called for existing entries. If you need to close resources, you'll have to implement
 * an explicit call to 'removeAll'. That will push all existing values to onRemove.
 *
 * IMPORTANT NOTE: Locking is not reentrant. As a result, onRemove is called AFTER the mutation lock is released, in
 * case the onRemove logic intends to call back into the cache. This may have unintended consequences if you're expecting
 * the mutation to be fully atomic in nature.
 */
class SharedLruCache<K, V>(private val maxCacheSize:Int, private val onRemove:(MutableMap.MutableEntry<K, V>) -> Unit = {}):LruCache<K, V>{

    init {
//        mpfreeze()
    }

    /**
     * Stores value at key.
     *
     * If replacing an existing value, that value will be returned, but onRemove will not
     * be called.
     *
     * If adding a new value, if the total number of values exceeds maxCacheSize, the last accessed
     * value will be removed and sent to onRemove.
     *
     * If adding a value with the same key and value of an existing value, the LRU cache is updated, but
     * the existing value is not returned. This effectively refreshes the entry in the LRU list.
     */
    override fun put(key: K, value: V): V? {
        var resultValue:V? = null
        val removeCollection: MutableList<MutableMap.MutableEntry<K, V>> = ArrayList()

        withLock {
            val cacheEntry = cacheMap.get(key)
            val node: AbstractSharedLinkedList.Node<K>
            val result: V?
            if (cacheEntry != null) {
                result = if (value != cacheEntry.v) {
                    cacheEntry.v
                } else {
                    null
                }
                node = cacheEntry.node
                node.readd()
            } else {
                result = null
                node = cacheList.addNode(key)
            }
            cacheMap.put(key, CacheEntry(value, node))

            while (cacheList.size > maxCacheSize){
                val key = cacheList.removeAt(0)
                val entry = cacheMap.remove(key)
                if(entry != null)
                    removeCollection.add(LruEntry(key, entry.v))
            }

            resultValue = result
        }

        removeCollection.forEach(onRemove)

        return resultValue
    }

    /**
     * Removes value at key (if it exists). If a value is found, it is passed to onRemove.
     */
    override fun remove(key: K) {
        var removeEntry: LruEntry<K, V>? = null
        withLock {
            val entry = cacheMap.remove(key)
            if (entry != null) {
                entry.node.remove()
                removeEntry = LruEntry(key, entry.v)
            }
        }
        if(removeEntry != null)
            onRemove(removeEntry!!)
    }

    /**
     * Returns all entries. This does not affect position in LRU cache. IE, old entries stay old.
     */
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = withLock {
            return internalAll()
        }

    /**
     * Clears the cache. If skipCallback is set to true, onRemove is not called. Defaults to false.
     */
    override fun removeAll(skipCallback:Boolean) {
        var removeCollection: Collection<MutableMap.MutableEntry<K, V>>? = null

        withLock {
            if (!skipCallback) {
                removeCollection = internalAll()
            }
            cacheMap.clear()
            cacheList.clear()
        }

        if(removeCollection != null){
            removeCollection!!.forEach(onRemove)
        }
    }

    /**
     * Finds and returns cache value, if it exists. If it exists, the key gets moved to the front of the
     * LRU list.
     */
    override fun get(key: K): V? = withLock {
        val cacheEntry = cacheMap.get(key)
        return if(cacheEntry != null){
            cacheEntry!!.node.readd()
            cacheEntry.v
        } else{
            null
        }
    }

    /*
    This was OK with Intellij but kicking back an llvm error. To investigate.
    override fun get(key: K): V? = withLock {
        val cacheEntry = cacheMap.get(key)
        if(cacheEntry != null){
            cacheEntry.node.readd()
            return cacheEntry.v
        }
        else{
            return null
        }
    }
     */

    /**
     * Well...
     */
    override fun exists(key: K): Boolean = withLock { cacheMap.get(key) != null }

    override val size: Int
        get() = withLock { cacheMap.size }

    data class CacheEntry<K, V>(val v:V, val node:AbstractSharedLinkedList.Node<K>)

    class LruEntry<K, V>(override val key: K, override val value: V):MutableMap.MutableEntry<K, V> {

        override fun setValue(newValue: V): V {
            throw UnsupportedOperationException()
        }

        override fun toString(): String {
            return "LruEntry(key=$key, value=$value)"
        }
    }

    private var lock: Lock = QuickLock()
    val cacheMap = SharedHashMap<K, CacheEntry<K, V>>(initialCapacity = maxCacheSize)
    val cacheList = SharedLinkedList<K>()

    private fun internalAll(): HashSet<MutableMap.MutableEntry<K, V>> {
        val set = HashSet<MutableMap.MutableEntry<K, V>>(cacheList.size)
        cacheList.iterator().forEach {
            set.add(LruEntry(it, cacheMap.get(it)!!.v))
        }
        return set
    }

    private inline fun <T> withLock(proc: () -> T): T {
        lock.lock()
        try {
            return proc()
        } finally {
            lock.unlock()
        }
    }

    internal fun printDebug(){
        println("CACHELIST")
        cacheList.forEach {
            println(it)
        }
        println("CACHEMAP")
        cacheMap.entries.forEach {
            println(it)
        }
    }
}

interface LruCache<K, V>{
    fun put(key:K, value:V):V?
    fun remove(key:K)
    val entries: MutableSet<MutableMap.MutableEntry<K, V>>
    fun removeAll(skipCallback:Boolean = false)
    fun get(key:K):V?
    fun exists(key:K):Boolean
    val size:Int
}

typealias LruEntry<K, V> = MutableMap.MutableEntry<K, V>