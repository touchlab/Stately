package co.touchlab.stately.collections

import co.touchlab.stately.ensureNeverFrozen
import co.touchlab.stately.freeze
import co.touchlab.stately.isolate.IsolateState
import co.touchlab.stately.isolate.StateHolder

class IsoLruCache<K, V>(
    private val maxCacheSize: Int,
    private val onRemove: (MutableMap.MutableEntry<K, V>) -> Unit = {}
) : IsolateState<IsoLruCache.LruState<K, V>>({ LruState() }), LruCache<K, V> {

    init {
        freeze()
    }

    class LruState<K, V> {
        val cacheMap :MutableMap<K, CacheEntry<K, V>>
        val cacheList :LruLinkedList<K>

        init {
            cacheMap = mutableMapOf()
            cacheMap.ensureNeverFrozen()
            cacheList = LruLinkedList()
            cacheList.ensureNeverFrozen()
        }
    }

    class LruLinkedList<T>() {
        var sizeCount = 0
        var head: Node<T>? = null
        var tail: Node<T>? = null

        fun toList(): List<T> {
            val l = mutableListOf<T>()
            var n = head
            while (n != null && n.nodeValue != null) {
                l.add(n.nodeValue!!)
                n = n.next
            }

            return l
        }

        fun add(element: T): Boolean {
            return internalAdd(makeNode(element))
        }

         fun add(index: Int, element: T) {
            if (index == sizeCount) {
                internalAdd(makeNode(element))
            } else {
                internalNodeAt(index).internalAdd(element)
            }
        }

        fun get(index: Int):T? { return internalNodeAt(index).nodeValue }

        private fun makeNode(t:T):Node<T>{
            val n = Node(this)
            n.nodeValue = t.freeze()
            n.ensureNeverFrozen()
            return n
        }

        fun addNode(element: T): Node<T> {
            val node = makeNode(element)
            internalAdd(node)
            return node
        }

        internal fun internalAdd(node: Node<T>): Boolean {
            node.checkNotRemoved()

            if (sizeCount == 0) {
                head = node
                tail = node
            } else {
                val prev = tail
                prev!!.next = node
                node.prev = prev
                tail = node
            }
            sizeCount++
            return true
        }

        internal fun internalRemoveAt(i: Int): T? {
            val node = internalNodeAt(i)
            val nodeValue = node.nodeValue
            node.internalRemove()
            return nodeValue
        }

        private fun internalNodeAt(i: Int): Node<T> {
            if (i >= sizeCount)
                throw IllegalArgumentException("index $i ge ${sizeCount}")

            var node = head
            var nodeCount = 0
            while (node != null) {
                if (i == nodeCount++) {
                    return node
                }
                node = node.next
            }

            throw IllegalStateException("Bad math")
        }

        fun clear() {
            internalClear()
        }

        private fun internalClear() {
            while (sizeCount != 0) {
                internalRemoveAt(0)
            }
            head = null
            tail = null
            sizeCount = 0
        }

        class IsoNode<T> internal constructor(stateHolder: StateHolder<Node<T>>) :
            IsolateState<Node<T>>(stateHolder) {

        }

        class Node<T>(val list: LruLinkedList<T>) {

            var nodeValue: T? = null

            var prev: Node<T>? = null
            var next: Node<T>? = null
            private var removed = false

            internal fun clearValue() {
                nodeValue = null
            }

            fun set(t: T) {
                internalSet(t)
            }

            /**
             * Set value
             *
             * Mutates list
             */
            internal fun internalSet(t: T) {
                checkNotRemoved()
                nodeValue = t.freeze()
            }

            /**
             * For iterators, make sure 'next' is always updated last so we don't need to lock nav.
             */
            fun add(t: T): Boolean = internalAdd(t)

            /**
             * Adds new value before me.
             *
             * Mutates list
             */
            internal fun internalAdd(t: T): Boolean {
                checkNotRemoved()
                val ins = list.makeNode(t)

                val prevNode = prev
                ins.prev = prevNode
                ins.next = this

                list.sizeCount++

                //If replacing 0
                if (prevNode == null) {
                    list.head = ins
                } else {
                    prevNode.next = ins
                }

                prev = ins

                return true
            }

            internal fun checkNotRemoved() {
                if (removed)
                    throw IllegalStateException("Node is removed $this")
            }

            /**
             * Add same node to end of list.
             */
            fun readd() {
                internalReadd()
            }

            /**
             * Re-add myself back to list.
             *
             * Mutates list
             */
            internal fun internalReadd() {
                checkNotRemoved()
                internalRemove(false)
                prev = null
                next = null
                list.internalAdd(this)
            }

            /**
             * For iterators, make sure 'next' is always updated last so we don't need to lock nav.
             */
            fun remove(permanent: Boolean = true) {
                internalRemove(permanent)
            }

            /**
             * Removes self from list.
             *
             * Mutates list
             */
            internal fun internalRemove(permanent: Boolean = true) {
                checkNotRemoved()

                if (permanent) {
                    removed = true
                    clearValue()
                }

                val prevNode = prev
                val nextNode = next

                list.sizeCount--

                if (nextNode == null) {
                    list.tail = prevNode
                } else {
                    nextNode.prev = prevNode
                }

                if (prevNode == null) {
                    list.head = nextNode
                } else {
                    prevNode.next = nextNode
                }
            }
        }
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
    override fun put(key: K, value: V): V? = access { lruState ->

        val removeCollection: MutableList<MutableMap.MutableEntry<K, V>> = ArrayList()

        val cacheEntry = lruState.cacheMap.get(key)
        val node: LruLinkedList.Node<K>
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
            node = lruState.cacheList.addNode(key)
        }

        lruState.cacheMap.put(key, CacheEntry(value, node))

        while (lruState.cacheList.sizeCount > maxCacheSize) {
            val key = lruState.cacheList.internalRemoveAt(0)
            val entry = lruState.cacheMap.remove(key)
            if (key != null && entry != null)
                removeCollection.add(LruEntry(key, entry.v))
        }

        removeCollection.forEach(onRemove)

        result
    }

    /**
     * Removes value at key (if it exists). If a value is found, it is passed to onRemove.
     */
    override fun remove(key: K, skipCallback: Boolean): V? = access { lruState ->
        var removeEntry: LruEntry<K, V>? = null

        val entry = lruState.cacheMap.remove(key)
        if (entry != null) {
            entry.node.remove()
            removeEntry = LruEntry(key, entry.v)
        }

        if (!skipCallback && removeEntry != null)
            onRemove(removeEntry!!)

        removeEntry?.value
    }

    /**
     * Returns all entries. This does not affect position in LRU cache. IE, old entries stay old.
     */
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = access {
            internalAll()
        }

    /**
     * Clears the cache. If skipCallback is set to true, onRemove is not called. Defaults to false.
     */
    override fun removeAll(skipCallback: Boolean) = access { lruState ->
        var removeCollection: Collection<MutableMap.MutableEntry<K, V>>? = null

        if (!skipCallback) {
            removeCollection = internalAll()
        }
        lruState.cacheMap.clear()
        lruState.cacheList.clear()

        if (removeCollection != null) {
            removeCollection!!.forEach(onRemove)
        }
    }

    /**
     * Finds and returns cache value, if it exists. If it exists, the key gets moved to the front of the
     * LRU list.
     */
    override fun get(key: K): V? = access { lruState ->
        val cacheEntry = lruState.cacheMap.get(key)
        if (cacheEntry != null) {
            cacheEntry.node.readd()
            cacheEntry.v
        } else {
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
    override fun exists(key: K): Boolean = access { lruState -> lruState.cacheMap.get(key) != null }

    override val size: Int
        get() = access { it.cacheMap.size }

    data class CacheEntry<K, V>(val v: V, val node: LruLinkedList.Node<K>)

    class LruEntry<K, V>(override val key: K, override val value: V) : MutableMap.MutableEntry<K, V> {

        override fun setValue(newValue: V): V {
            throw UnsupportedOperationException()
        }

        override fun toString(): String {
            return "LruEntry(key=$key, value=$value)"
        }
    }

    private fun internalAll(): HashSet<MutableMap.MutableEntry<K, V>> = access { lruState ->
        val set = HashSet<MutableMap.MutableEntry<K, V>>(lruState.cacheList.sizeCount)
        lruState.cacheList.toList().forEach {
            set.add(LruEntry(it, lruState.cacheMap.get(it)!!.v))
        }
        set
    }
}

interface LruCache<K, V> {
    fun put(key: K, value: V): V?
    fun remove(key: K, skipCallback: Boolean = false): V?
    val entries: MutableSet<MutableMap.MutableEntry<K, V>>
    fun removeAll(skipCallback: Boolean = false)
    fun get(key: K): V?
    fun exists(key: K): Boolean
    val size: Int
}

typealias LruEntry<K, V> = MutableMap.MutableEntry<K, V>