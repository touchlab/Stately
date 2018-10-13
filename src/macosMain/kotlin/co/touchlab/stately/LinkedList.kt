package co.touchlab.stately

import platform.Foundation.NSLock
import kotlin.native.concurrent.AtomicInt
import kotlin.native.concurrent.AtomicReference
import kotlin.native.concurrent.freeze

/**
 * Linked List (doubly link). A linked list is a data structure consisting
 * of a group of nodes which together represent a sequence.
 *
 *
 * @see [Linked List
 * @author Justin Wetherell <phishman3579></phishman3579>@gmail.com>
](https://en.wikipedia.org/wiki/Linked_list) */
class DoublyLinkedList<T> {

    val size: Int
        get() = sizeCount.value

    fun add(element: T): Boolean = withLock {
        return internalAdd(Node(this, element))
    }

    fun add(index: Int, element: T) = withLock {
        if (index == sizeCount.value) {
            return internalAdd(Node(this, element))
        } else {
            internalNodeAt(index).add(element)
        }
    }

    fun addAll(index: Int, elements: Collection<T>): Boolean = withLock {
        if (index == size)
            return internalAddAll(elements)
        else if (index > size)
            throw IndexOutOfBoundsException("Index $index > $size")
        else {
            var node = internalNodeAt(index)
            elements.forEach {
                node.add(it)
            }
            return true
        }
    }

    fun addAll(elements: Collection<T>): Boolean = withLock { internalAddAll(elements) }

    fun clear() = withLock {
        head.value = null
        tail.value = null
        sizeCount.value = 0
    }

    fun contains(element: T): Boolean = withLock { internalFindFirst(element) != null }

    fun containsAll(elements: Collection<T>): Boolean = withLock { elements.all { internalFindFirst(it) != null } }

    fun get(index: Int) = withLock { internalNodeAt(index).nodeValue }

    fun indexOf(element: T): Int = withLock { internalFindFirstIndex(element).index }

    fun isEmpty() = sizeCount.value == 0

    fun remove(value: T): Boolean = withLock { internalRemove(value) }

    fun removeAll(elements: Collection<T>): Boolean = withLock { elements.all { internalRemove(it) } }

    fun removeAt(index: Int):T = withLock {
        val node = internalNodeAt(index)
        node.remove()
        node.nodeValue
    }

    fun set(index: Int, element: T): T = withLock {
        val node = internalNodeAt(index)
        val old = node.nodeValue
        node.set(element)

        return old
    }

    class Node<T>(val list: DoublyLinkedList<T>, val nodeValue: T) {

        val prev = AtomicReference<Node<T>?>(null)
        val next = AtomicReference<Node<T>?>(null)

        fun set(t: T) {
            val ins = Node(list, t)

            ins.freeze()

            ins.prev.value = prev.value
            ins.next.value = next.value

            if (ins.prev.value == null)
                list.head.value = ins
            if (ins.next.value == null)
                list.tail.value = ins
        }

        fun add(t: T): Boolean {
            val ins = Node(list, t)

            ins.freeze()

            ins.prev.value = prev.value
            ins.next.value = this
            prev.value?.next?.value = ins
            prev.value = ins

            //If replacing 0
            if (ins.prev.value == null)
                list.head.value = ins

            list.sizeCount.increment()

            return true
        }

        fun remove() {
            val prevNode = prev.value
            val nextNode = next.value
            if (prevNode == null) {
                list.head.value = nextNode
            } else {
                prevNode.next.value = nextNode
            }

            if (nextNode == null) {
                list.tail.value = prevNode
            } else {
                nextNode.prev.value = prevNode
            }

            list.sizeCount.decrement()
        }
    }

    private val lock = NSLock()
    private val sizeCount = AtomicInt(0)
    private val head = AtomicReference<Node<T>?>(null)
    private val tail = AtomicReference<Node<T>?>(null)

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
        node.freeze()
        if (isEmpty()) {
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

    fun toList(): List<T> {
        val outlist = ArrayList<T>(sizeCount.value)
        var node = head.value
        while (node != null) {
            outlist.add(node.nodeValue)
            node = node.next.value
        }

        return outlist
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


    private inline fun <T> withLock(proc: () -> T): T {
        lock.lock()
        try {
            return proc.invoke()
        } finally {
            lock.unlock()
        }
    }
}

