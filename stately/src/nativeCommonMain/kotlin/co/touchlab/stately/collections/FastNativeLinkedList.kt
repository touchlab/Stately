package co.touchlab.stately.collections

import kotlin.native.concurrent.AtomicInt
import kotlin.native.concurrent.freeze

public fun <T> nativeListOf(vararg elements: T): List<T> {
    val l = FastNativeLinkedList<T>()
    l.addAll(elements)
    return l
}

public fun <T> nativeEmptyList(): List<T> = FastNativeLinkedList<T>()

public fun <T> Collection<T>.toNativeMutableList(): MutableList<T> {
    val l = FastNativeLinkedList<T>()
    l.addAll(this)
    return l
}

public inline fun <T> NativeMutableList(size: Int, init: (index: Int) -> T): MutableList<T> {
    val list = FastNativeLinkedList<T>()
    repeat(size) { index -> list.add(init(index)) }
    return list
}

class FastNativeLinkedList<T> : MutableList<T> {

    internal val version = AtomicInt(0)
    private val nativePtr = nativeListCreate()

    init {
        freeze()
    }

    internal fun debugPrint():String{
        return "list vals: ${joinToString()}"
    }
    override val size: Int
        get() = nativeListSize(nativePtr)

    override fun add(element: T): Boolean = withLock {
        nativeListAdd(nativePtr, element.freeze())
        version.increment()
        return true
    }

    internal fun addNode(element: T): Node = withLock {
        val node = Node(nativeListAddNode(nativePtr, element.freeze()) as NativeMemory).freeze()
        version.increment()
        return node
    }

    override fun add(index: Int, element: T) {
        withLock {
            when {
                index == size -> add(element.freeze())
                index > size -> throw IndexOutOfBoundsException()
                else -> {
                    val listIterator = listIterator(index)
                    listIterator.next()
                    listIterator.add(element.freeze())
                }
            }
        }
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean = withLock {
        when {
            index == size -> addAll(elements)
            index > size -> throw IndexOutOfBoundsException()
            else -> {
                val listIterator = listIterator(index)
                listIterator.next()
                elements.reversed().iterator().forEach { listIterator.add(it.freeze()) }
            }
        }
        true
    }

    override fun addAll(elements: Collection<T>): Boolean = withLock {
        elements.forEach {
            add(it)
        }
        true
    }

    override fun clear() {
        version.increment()
        nativeListClear(nativePtr)
    }

    override fun contains(element: T): Boolean = withLock { any { it == element } }

    override fun containsAll(elements: Collection<T>): Boolean = withLock {
        elements.all { element ->
            this@FastNativeLinkedList.any { it == element }
        }
    }

    override fun get(index: Int): T = withLock { listIterator(index).next() }

    override fun indexOf(element: T): Int = withLock {
        val iterator = listIterator()
        var count = 0
        iterator.forEach {
            if (it == element)
                return@withLock count
            count++
        }

        -1
    }

    override fun isEmpty(): Boolean = size == 0

    override fun iterator(): MutableIterator<T> = ListIterator(nativeListBeginIter(nativePtr) as NativeMemory)

    override fun lastIndexOf(element: T): Int = withLock {
        val iterator = listIterator()
        var count = 0
        var lastMatch = -1
        iterator.forEach {
            if (it == element)
                lastMatch = count
            count++
        }

        lastMatch
    }

    override fun listIterator(): MutableListIterator<T> = ListIterator(nativeListBeginIter(nativePtr) as NativeMemory)

    override fun listIterator(index: Int): MutableListIterator<T> = withLock {
        if (index < 0 || index >= size)
            throw IndexOutOfBoundsException()

        val iter = listIterator()
        var count = 0
        while (count < index) {
            iter.next()
            count++
        }

        iter
    }

    override fun remove(element: T): Boolean = withLock {
        val iterator = listIterator()
        iterator.forEach {
            if (it == element) {
                iterator.remove()
                return@withLock true
            }
        }

        false
    }

    override fun removeAll(elements: Collection<T>): Boolean = withLock {
        elements.map { remove(it) }.any { it }
    }

    override fun removeAt(index: Int): T = withLock {
        if (index >= size)
            throw IndexOutOfBoundsException()

        val iterator = listIterator(index)
        val result = iterator.next()
        iterator.remove()
        result
    }

    override fun retainAll(elements: Collection<T>): Boolean = withLock {
        val iter = listIterator()
        var anyRemoved = false
        iter.forEach {
            if (!elements.contains(it)) {
                iter.remove()
                anyRemoved = true
            }
        }

        anyRemoved
    }

    override fun set(index: Int, element: T): T = withLock {
        val iterator = listIterator(index)
        val result = iterator.next()
        iterator.set(element.freeze())
        result
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun equals(other: Any?): Boolean {
        return other === this ||
                (other is List<*>) && contentEquals(other)
    }

    override fun hashCode(): Int = withLock {
        val iter = listIterator()
        var result = 1
        var i = 0

        while (iter.hasNext()) {
            val nextElement = iter.next()
            val nextHash = if (nextElement != null) nextElement.hashCode() else 0
            result = result * 31 + nextHash
            i++
        }
        return result
    }

    /*override fun toString(): String {
        return this.array.subarrayContentToString(offset, length)
    }*/

    private fun contentEquals(other: List<*>): Boolean = withLock {

        fun compare(a: List<*>, b: List<*>): Boolean {
            if (a.size != b.size) return false
            val myIter = a.listIterator()
            val otherIter = b.listIterator()
            while (otherIter.hasNext() && myIter.hasNext()) {
                if (myIter.next() != otherIter.next()) return false
            }
            return true
        }

        if (other is FastNativeLinkedList<*>) {
            other.withLock {
                compare(this, other)
            }
        } else {
            compare(this, other)
        }
    }

    internal inner class Node(private val nativeMemory: NativeMemory){
        fun remove(){
            nativeListNodeRemove(nativePtr, nativeMemory)
        }

        fun readd(){
            nativeListNodeReadd(nativePtr, nativeMemory)
        }
    }

    internal inner class ListIterator(private val nativeMemory: NativeMemory) : MutableListIterator<T> {
        private val myVersion = AtomicInt(version.value)
        private val atomicIndex = AtomicInt(0)

        private fun incrementVersions(){
            myVersion.increment()
            version.increment()
        }

        private fun checkVersions(){
            if(myVersion.value != version.value)
                throw ConcurrentModificationException()
        }

        override fun hasPrevious(): Boolean = nativeListIterHasPrevious(nativePtr, nativeMemory)

        override fun nextIndex(): Int = atomicIndex.value + 1

        override fun previous(): T = withLock {
            checkVersions()
            val prev = nativeListIterPrevious(nativePtr, nativeMemory)
            atomicIndex.decrement()
            prev as T
        }

        override fun previousIndex(): Int = atomicIndex.value - 1

        override fun add(element: T) = withLock{
            checkVersions()
            incrementVersions()
            nativeListIterAdd(nativePtr, nativeMemory, element.freeze())
        }

        override fun hasNext(): Boolean = nativeListIterHasNext(nativePtr, nativeMemory)

        override fun next(): T = withLock {
            checkVersions()
            val next = nativeListIterNext(nativePtr, nativeMemory)
            atomicIndex.increment()
            next as T
        }

        override fun remove() = withLock{
            checkVersions()
            incrementVersions()
            nativeListIterRemove(nativePtr, nativeMemory)
        }

        override fun set(element: T) = withLock{
            checkVersions()
            incrementVersions()
            nativeListIterSet(nativePtr, nativeMemory, element.freeze())
        }
    }

    internal inline fun <R> withLock(block: FastNativeLinkedList<T>.() -> R): R {
        nativeListLock(nativePtr)
        try {
            return block()
        } finally {
            nativeListUnlock(nativePtr)
        }
    }

    override fun toString(): String {
        return "[${joinToString()}]"
    }
}

@SymbolName("Stately_list_create")
private external fun nativeListCreate(): Long

@SymbolName("Stately_lest_size")
private external fun nativeListSize(ptr: Long): Int

@SymbolName("Stately_list_add")
private external fun nativeListAdd(ptr: Long, value: Any?)

@SymbolName("Stately_list_addNode")
private external fun nativeListAddNode(ptr: Long, value: Any?): Any?

@SymbolName("Stately_list_nodeRemove")
private external fun nativeListNodeRemove(ptr: Long, nativeMemory: NativeMemory)

@SymbolName("Stately_list_nodeReadd")
private external fun nativeListNodeReadd(ptr: Long, nativeMemory: NativeMemory)

@SymbolName("Stately_list_clear")
private external fun nativeListClear(ptr: Long)

@SymbolName("Stately_list_beginIter")
private external fun nativeListBeginIter(ptr: Long): Any?

@SymbolName("Stately_list_iterHasPrevious")
private external fun nativeListIterHasPrevious(ptr: Long, nativeMemory: NativeMemory): Boolean

@SymbolName("Stately_list_iterHasNext")
private external fun nativeListIterHasNext(ptr: Long, nativeMemory: NativeMemory): Boolean

@SymbolName("Stately_list_iterPrevious")
private external fun nativeListIterPrevious(ptr: Long, nativeMemory: NativeMemory): Any?

@SymbolName("Stately_list_iterNext")
private external fun nativeListIterNext(ptr: Long, nativeMemory: NativeMemory): Any?

@SymbolName("Stately_list_iterAdd")
private external fun nativeListIterAdd(ptr: Long, nativeMemory: NativeMemory, value: Any?)

@SymbolName("Stately_list_iterRemove")
private external fun nativeListIterRemove(ptr: Long, nativeMemory: NativeMemory)

@SymbolName("Stately_list_iterSet")
private external fun nativeListIterSet(ptr: Long, nativeMemory: NativeMemory, value: Any?)

@SymbolName("Stately_list_lock")
private external fun nativeListLock(ptr: Long)

@SymbolName("Stately_list_unlock")
private external fun nativeListUnlock(ptr: Long)