package co.touchlab.stately.collections

import kotlin.native.concurrent.AtomicInt
import kotlin.native.concurrent.freeze

class FastNativeLinkedList<T> : MutableList<T> {
    private val nativePtr = nativeListCreate()

    override val size: Int
        get() = nativeListSize(nativePtr)

    override fun add(element: T): Boolean {
        nativeListAdd(nativePtr, element.freeze())
        return true
    }

    override fun add(index: Int, element: T) {
        withLock {
            when {
                index == size -> add(element)
                index > size -> throw IndexOutOfBoundsException()
                else -> listIterator(index).add(element.freeze())
            }
        }
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean = withLock {
        if (index > size)
            throw IndexOutOfBoundsException()

        val iter = listIterator(index)
        elements.reversed().iterator().forEach { iter.add(it.freeze()) }
        true
    }

    override fun addAll(elements: Collection<T>): Boolean = withLock {
        elements.forEach {
            add(it)
        }
        true
    }

    override fun clear() {
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

    override fun removeAll(elements: Collection<T>): Boolean = withLock { elements.map { remove(it) }.any { it } }

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



    internal inner class ListIterator(private val nativeMemory: NativeMemory) : MutableListIterator<T> {
        private val atomicIndex = AtomicInt(0)

        override fun hasPrevious(): Boolean = nativeListIterHasPrevious(nativePtr, nativeMemory)

        override fun nextIndex(): Int = atomicIndex.value + 1

        override fun previous(): T = withLock {
            val prev = nativeListIterPrevious(nativePtr, nativeMemory)
            atomicIndex.decrement()
            prev as T
        }

        override fun previousIndex(): Int = atomicIndex.value - 1

        override fun add(element: T) {
            nativeListIterAdd(nativePtr, nativeMemory, element.freeze())
        }

        override fun hasNext(): Boolean = nativeListIterHasNext(nativePtr, nativeMemory)

        override fun next(): T = withLock {
            val next = nativeListIterNext(nativePtr, nativeMemory)
            atomicIndex.increment()
            next as T
        }

        override fun remove() {
            nativeListIterRemove(nativePtr, nativeMemory)
        }

        override fun set(element: T) {
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