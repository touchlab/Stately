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

    class Node<T>(val list:DoublyLinkedList<T>, val nodeValue: T) {

        val prev = AtomicReference<Node<T>?>(null)
        val next = AtomicReference<Node<T>?>(null)

        fun add(t:T):Boolean{
            val ins = Node<T>(list, t)

            ins.freeze()

            ins.prev.value = prev.value
            ins.next.value = this
            prev.value?.next?.value = ins
            prev.value = ins

            //If replacing 0
            if(ins.prev.value == null)
                list.head.value = ins

            list.size.increment()

            return true
        }

        fun remove(){
            val prevNode = prev.value
            val nextNode = next.value
            if(prevNode == null)
            {
                list.head.value = nextNode
            }
            else
            {
                prevNode.next.value = nextNode
            }

            if(nextNode == null)
            {
                list.tail.value = prevNode
            }
            else
            {
                nextNode.prev.value = prevNode
            }

            list.size.decrement()
        }
    }

    private val lock = NSLock()
    private val size = AtomicInt(0)
    private val head = AtomicReference<Node<T>?>(null)
    private val tail = AtomicReference<Node<T>?>(null)

    /**
     * {@inheritDoc}
     */
    fun add(value: T): Boolean {
        return add(Node(this, value))
    }

    fun nodeAt(i:Int):Node<T>{
        if(i>=size.value)
            throw IllegalArgumentException("index $i ge ${size.value}")

        var node = head.value
        var nodeCount = 0
        while (node != null) {
            if(i == nodeCount++)
                return node
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
    private fun add(node: Node<T>): Boolean {
        node.freeze()
        if (isEmpty) {
            head.value = node
            tail.value = node
        } else {
            val prev = tail.value
            prev!!.next.value = node
            node.prev.value = prev
            tail.value = node
        }
        size.increment()
        return true
    }

    private val isEmpty:Boolean
    get() = head.value == null


    /**
     * {@inheritDoc}
     */
    fun remove(value: T): Boolean {
        val node = findFirst(value)
        return if(node == null){
            false
        }else{
            node.remove()
            true
        }
    }

    fun toList():List<T> {
        val outlist = ArrayList<T>(size.value)
        var node = head.value
        while (node != null) {
            outlist.add(node.nodeValue)
            node = node.next.value
        }

        return outlist
    }

    /**
     * {@inheritDoc}
     */
    fun clear() {
        head.value = null
        size.value = 0
    }

    /**
     * {@inheritDoc}
     */
    fun contains(value: T): Boolean = findFirst(value) != null

    private fun findFirst(value: T): Node<T>?{
        var node = head.value
        while (node != null) {
            if(node.nodeValue == value){
                return node
            }
            node = node.next.value
        }

        return null
    }

    /**
     * {@inheritDoc}
     */
    fun size(): Int {
        return size.value
    }

    fun debugPrint():String{
        val sb = StringBuilder()
        var node = head.value

        while (node != null){
            sb.append("val: ${node.nodeValue}, pref: ${node.prev.value}, next: ${node.next.value}\n")
            node = node.next.value
        }

        return sb.toString()
    }

    /*private inline fun <T> withLock(proc:() -> T):T{
        lock.lock()
        try {
            return proc.invoke()
        } finally {
            lock.unlock()
        }
    }*/

    /**
     * {@inheritDoc}
     */
   /* fun validate(): Boolean {
        val keys = java.util.HashSet()
        val node = head
        if (node != null) {
            keys.add(node.value)
            if (node.prev != null)
                return false
            var child = node.next
            while (child != null) {
                if (!validate(child, keys))
                    return false
                child = child.next
            }
        }
        return keys.size == size
    }*/

    /*private fun validate(node: Node<T>, keys: MutableSet<T>): Boolean {
        if (node.value == null)
            return false

        keys.add(node.value)

        val child = node.next
        if (child != null) {
            if (child.prev != node)
                return false
        } else {
            if (node != tail)
                return false
        }
        return true
    }*/
}

