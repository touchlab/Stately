package co.touchlab.stately.collections

import co.touchlab.stately.concurrency.Lock
import co.touchlab.stately.concurrency.QuickLock

typealias LruEntry<K, V> = MutableMap.MutableEntry<K, V>

/**
 * onRemove should be very careful not to wind up calling into the cache. The lock is not reentrant.
 */
class SharedLruCache<K, V>(private val maxCacheSize:Int, private val onRemove:(LruEntry<K, V>) -> Unit = {}):LruCache<K, V>{

    override fun put(key: K, value: V):V? = withLock{
        val cacheEntry = cacheMap.get(key)
        val node:AbstractSharedLinkedList.Node<K>
        val result:V?
        if(cacheEntry != null) {
            result = cacheEntry.v
            node = cacheEntry.node
            node.readd()
        }
        else {
            result = null
            node = cacheList.addNode(key)
        }
        cacheMap.put(key, CacheEntry(value, node))

        trimIfNeeded()

        return result
    }

    override fun remove(key: K) = withLock{
        val entry = cacheMap.get(key)
        if(entry != null)
        {
            entry.node.remove()
            onRemove(OutEntry(key, entry.v))
        }
    }

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = withLock {
            return internalAll()
        }

    override fun removeAll() = withLock {
        val entries = internalAll()
        entries.forEach(onRemove)
        cacheMap.clear()
        cacheList.clear()
    }

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

    override fun exists(key: K): Boolean = cacheMap.get(key) != null

    override val size: Int
        get() = cacheMap.size

    data class CacheEntry<K, V>(val v:V, val node:AbstractSharedLinkedList.Node<K>)

    class OutEntry<K, V>(override val key: K, override val value: V):MutableMap.MutableEntry<K, V> {
        override fun setValue(newValue: V): V {
            throw UnsupportedOperationException()
        }
    }

    private var lock: Lock = QuickLock()
    val cacheMap = SharedHashMap<K, CacheEntry<K, V>>(initialCapacity = maxCacheSize)
    val cacheList = SharedLinkedList<K>()

    private fun trimIfNeeded(){
        while (cacheList.size > maxCacheSize){
            val key = cacheList.removeAt(0)
            val entry = cacheMap.remove(key)
            if(entry != null)
                onRemove(OutEntry(key, entry.v))
        }
    }

    private fun internalAll(): HashSet<LruEntry<K, V>> {
        val set = HashSet<LruEntry<K, V>>(cacheList.size)
        cacheList.iterator().forEach {
            set.add(OutEntry(it, cacheMap.get(it)!!.v))
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
}

interface LruCache<K, V>{
    fun put(key:K, value:V):V?
    fun remove(key:K)
    val entries: MutableSet<MutableMap.MutableEntry<K, V>>
    fun removeAll()
    fun get(key:K):V?
    fun exists(key:K):Boolean
    val size:Int
}