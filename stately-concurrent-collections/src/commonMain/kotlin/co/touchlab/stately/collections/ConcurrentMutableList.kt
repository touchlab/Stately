package co.touchlab.stately.collections

import co.touchlab.stately.concurrency.Synchronizable
import co.touchlab.stately.concurrency.synchronize

class ConcurrentMutableList<E> internal constructor(rootArg: Synchronizable?, private val del: MutableList<E>) :
    ConcurrentMutableCollection<E>(rootArg, del), MutableList<E> {
    constructor() : this(null, mutableListOf())

    override fun get(index: Int): E = syncTarget.synchronize { del.get(index) }

    override fun indexOf(element: E): Int = syncTarget.synchronize { del.indexOf(element) }

    override fun lastIndexOf(element: E): Int = syncTarget.synchronize { del.lastIndexOf(element) }

    override fun add(index: Int, element: E) {
        syncTarget.synchronize { del.add(index, element) }
    }

    override fun addAll(index: Int, elements: Collection<E>): Boolean =
        syncTarget.synchronize { del.addAll(index, elements) }

    override fun listIterator(): MutableListIterator<E> =
        syncTarget.synchronize { ConcurrentMutableListIterator(this, del.listIterator()) }

    override fun listIterator(index: Int): MutableListIterator<E> =
        syncTarget.synchronize { ConcurrentMutableListIterator(this, del.listIterator(index)) }

    override fun removeAt(index: Int): E = syncTarget.synchronize { del.removeAt(index) }

    override fun set(index: Int, element: E): E = syncTarget.synchronize { del.set(index, element) }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> =
        syncTarget.synchronize { ConcurrentMutableList(this, del.subList(fromIndex, toIndex)) }

    fun <R> block(f: (MutableList<E>) -> R): R = syncTarget.synchronize {
        val wrapper = MutableListWrapper(del)
        val result = f(wrapper)
        wrapper.list = mutableListOf()
        result
    }
}

internal class MutableListWrapper<E>(internal var list: MutableList<E>) : MutableCollectionWrapper<E>(list),
    MutableList<E> {
    override fun get(index: Int): E = list.get(index)

    override fun indexOf(element: E): Int = list.indexOf(element)

    override fun lastIndexOf(element: E): Int = list.lastIndexOf(element)

    override fun add(index: Int, element: E) = list.add(index, element)

    override fun addAll(index: Int, elements: Collection<E>): Boolean = list.addAll(index, elements)

    override fun listIterator(): MutableListIterator<E> = list.listIterator()

    override fun listIterator(index: Int): MutableListIterator<E> = list.listIterator(index)

    override fun removeAt(index: Int): E = list.removeAt(index)

    override fun set(index: Int, element: E): E = list.set(index, element)

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> = list.subList(fromIndex, toIndex)
}