package co.touchlab.stately.collections

import co.touchlab.stately.concurrency.AtomicInt
import co.touchlab.stately.concurrency.AtomicReference
import co.touchlab.stately.concurrency.Lock
import co.touchlab.stately.concurrency.QuickLock

/**
 * Thread safe linked list implementation for Kotlin Multiplatform. This is intended for situations where there may
 * be fairly regular updates and/or relatively large sizes. List iterators are mutable, and if the list changes
 * while iterating, you won't have concurrent modification exceptions, but obviously the state of the list from
 * when you started iterating is not guaranteed.
 */
class SharedLinkedList<T>():AbstractSharedLinkedList<T>(){
    override fun updated() {
        //Meh
    }

    override fun listIterator(): MutableListIterator<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun listIterator(index: Int): MutableListIterator<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun iterator(): MutableIterator<T> = LLIterator(this)

    class LLIterator<T>(ll: SharedLinkedList<T>) : MutableIterator<T> {
        override fun remove() {
            throw UnsupportedOperationException()
        }

        var currentNode = ll.head.value
        override fun hasNext(): Boolean = currentNode?.nodeValue != null

        override fun next(): T {
            val retval: T
            retval = currentNode?.nodeValue!!
            currentNode = currentNode?.next?.value
            return retval
        }
    }
}

/**
 * Fairly heavy handed implementation if list sizes are relatively significant. When stable copies are read,
 * if the list has been updated, the list will lock while a copy is made. If the list is updated often,
 * this is better than the simpler CopyOnWriteList, because EVERY update will lock and save, but something
 * to keep in mind. If updating the list while you're iterating isn't a huge problem, use SharedLinkedList
 */
class CopyOnWriteLinkedList<T>():AbstractSharedLinkedList<T>(){
    override fun updated() {
        updated.value = 1
    }

    override fun iterator(): MutableIterator<T> = withLock{
        checkUpdate()
        lastList.value.iterator()
    }

    override fun listIterator(): MutableListIterator<T> = withLock{
        checkUpdate()
        lastList.value.listIterator()
    }

    override fun listIterator(index: Int): MutableListIterator<T> = withLock{
        checkUpdate()
        lastList.value.listIterator(index)
    }

    private fun checkUpdate(){
        if(updated.value != 0)
        {
            val newList = ArrayList<T>(sizeCount.value)
            var node = head.value
            var nodeCount = 0
            while (node != null) {
                newList.add(node.nodeValue)
                node = node.next.value
                nodeCount++
            }

            lastList.value = newList.mpfreeze()
        }
    }

    private val updated = AtomicInt(0)
    private val lastList = AtomicReference<MutableList<T>>(mutableListOf())

}

abstract class AbstractSharedLinkedList<T>():MutableList<T> {
    override fun lastIndexOf(element: T): Int = withLock {
        var lastIndex = -1
        var indexCounter = 0
        iterator().forEach {
            if(it == element)
                lastIndex = indexCounter

            indexCounter++
        }

        lastIndex
    }

    override fun retainAll(elements: Collection<T>): Boolean = withLock {
        val result:ArrayList<T> = ArrayList<T>(sizeCount.value)
        iterator().forEach {
            if(elements.contains(it))
                result.add(it)
        }

        internalClear()
        result.forEach {
            internalAdd(Node(this, it))
        }

        true
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> {
        throw UnsupportedOperationException()
    }

    override val size: Int
        get() = withLock { sizeCount.value }

    override fun add(element: T): Boolean = withLock {
        return internalAdd(Node(this, element))
    }

    override fun add(index: Int, element: T) {
        withLock {
            if (index == sizeCount.value) {
                internalAdd(Node(this, element))
            } else {
                internalNodeAt(index).add(element)
            }
        }
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean = withLock {
        return when {
            index == sizeCount.value -> internalAddAll(elements)
            index > sizeCount.value -> throw IndexOutOfBoundsException("Index $index > ${sizeCount.value}")
            else -> {
                val node = internalNodeAt(index)
                elements.forEach {
                    node.add(it)
                }
                true
            }
        }
    }

    override fun addAll(elements: Collection<T>): Boolean = withLock { internalAddAll(elements) }

    override fun clear() = withLock {
        internalClear()
    }

    private fun internalClear() {
        head.value = null
        tail.value = null
        sizeCount.value = 0
    }

    override fun contains(element: T): Boolean = withLock { internalFindFirst(element) != null }

    override fun containsAll(elements: Collection<T>): Boolean = withLock { elements.all { internalFindFirst(it) != null } }

    override fun get(index: Int) = withLock { internalNodeAt(index).nodeValue }

    override fun indexOf(element: T): Int = withLock { internalFindFirstIndex(element).index }

    override fun isEmpty() = withLock { sizeCount.value == 0 }

    override fun remove(value: T): Boolean = withLock { internalRemove(value) }

    override fun removeAll(elements: Collection<T>): Boolean = withLock { elements.all { internalRemove(it) } }

    override fun removeAt(index: Int): T = withLock {
        val node = internalNodeAt(index)
        node.remove()
        node.nodeValue
    }

    override fun set(index: Int, element: T): T = withLock {
        val node = internalNodeAt(index)
        val old = node.nodeValue
        node.set(element)

        return old
    }

    class Node<T>(val list: AbstractSharedLinkedList<T>, val nodeValue: T) {

        val prev = AtomicReference<Node<T>?>(null)
        val next = AtomicReference<Node<T>?>(null)

        fun set(t: T) {
            val ins = Node(list, t)

            ins.mpfreeze()

            val prevNode = prev.value
            val nextNode = next.value

            ins.prev.value = prevNode
            ins.next.value = nextNode

            if (nextNode == null) {
                list.tail.value = ins
            } else {
                nextNode.prev.value = ins
            }

            if (prevNode == null) {
                list.head.value = ins
            } else {
                prevNode.next.value = ins
            }
        }

        /**
         * For iterators, make sure 'next' is always updated last so we don't need to lock nav.
         */
        fun add(t: T): Boolean {
            val ins = Node(list, t)

            ins.mpfreeze()

            val prevNode = prev.value
            ins.prev.value = prevNode
            ins.next.value = this

            list.sizeCount.increment()

            //If replacing 0
            if (prevNode == null){
                list.head.value = ins
            }
            else{
                prevNode.next.value = ins
                prev.value = ins
            }

            return true
        }

        /**
         * For iterators, make sure 'next' is always updated last so we don't need to lock nav.
         */
        fun remove() {
            val prevNode = prev.value
            val nextNode = next.value

            list.sizeCount.decrement()

            if (nextNode == null) {
                list.tail.value = prevNode
            } else {
                nextNode.prev.value = prevNode
            }

            if (prevNode == null) {
                list.head.value = nextNode
            } else {
                prevNode.next.value = nextNode
            }
        }
    }

    private var lock: Lock = QuickLock()
    internal val sizeCount = AtomicInt(0)
    internal val head = AtomicReference<Node<T>?>(null)
    internal val tail = AtomicReference<Node<T>?>(null)

    internal fun internalAddAll(elements: Collection<T>): Boolean {
        elements.forEach {
            internalAdd(Node(this, it))
        }

        return true
    }

    internal fun internalNodeAt(i: Int): Node<T> {
        if (i >= sizeCount.value)
            throw IllegalArgumentException("index $i ge ${sizeCount.value}")

        var node = head.value
        var nodeCount = 0
        while (node != null) {
            if (i == nodeCount++) {
                return node
            }
            node = node.next.value
        }

        throw IllegalStateException("Bad math")
    }

    /**
     * Add node to list.
     *
     * @param node
     * to add to list.
     */
    internal fun internalAdd(node: Node<T>): Boolean {
        node.mpfreeze()
        if (sizeCount.value == 0) {
            head.value = node
            tail.value = node
        } else {
            val prev = tail.value
            prev!!.next.value = node
            node.prev.value = prev
            tail.value = node
        }
        sizeCount.increment()
        return true
    }

    internal fun internalFindFirstIndex(value: T): NodeResult<T> {
        var node = head.value
        var nodeCount = 0
        while (node != null) {
            if (node.nodeValue == value) {
                return NodeResult(nodeCount, node)
            }
            node = node.next.value
            nodeCount++
        }

        return NodeResult(-1, null)
    }

    internal fun internalFindFirst(value: T): Node<T>? = internalFindFirstIndex(value).node

    internal fun internalRemove(value: T): Boolean {
        val node = internalFindFirst(value)
        return if (node == null) {
            false
        } else {
            node.remove()
            true
        }
    }

    data class NodeResult<T>(val index: Int, val node: Node<T>?)

    fun debugPrint(): String {
        val sb = StringBuilder()
        var node = head.value

        while (node != null) {
            sb.append("val: ${node.nodeValue}, pref: ${node.prev.value}, next: ${node.next.value}\n")
            node = node.next.value
        }

        return sb.toString()
    }

    internal fun lock(){
        lock.lock()
    }

    internal fun unlock(){
        lock.unlock()
    }

    internal abstract fun updated()

    internal inline fun <T> withLock(updated:Boolean = true, proc: () -> T): T {
        lock()
        try {
            return proc.invoke()
        } finally {
            if(updated)
                updated()
            unlock()
        }
    }
}