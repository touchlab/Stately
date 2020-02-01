package co.touchlab.stately.collections

import co.touchlab.stately.isolate.IsolateState
import co.touchlab.stately.isolate.StateHolder
import co.touchlab.stately.isolate.createState

open class IsoMutableList<T> internal constructor(stateHolder: StateHolder<MutableList<T>>) :
    IsoMutableCollection<T>(stateHolder), MutableList<T> {
    constructor(producer: () -> MutableList<T> = { mutableListOf() }) : this(createState(producer))

    override fun get(index: Int): T = asAccess { it.get(index) }
    override fun indexOf(element: T): Int = asAccess { it.indexOf(element) }
    override fun lastIndexOf(element: T): Int = asAccess { it.lastIndexOf(element) }
    override fun add(index: Int, element: T) = asAccess { it.add(index, element) }
    override fun addAll(index: Int, elements: Collection<T>): Boolean =
        asAccess { it.addAll(index, elements) }
    override fun listIterator(): MutableListIterator<T> =
        asAccess { IsoMutableListIterator(fork(it.listIterator())) }
    override fun listIterator(index: Int): MutableListIterator<T> =
        asAccess { IsoMutableListIterator(fork(it.listIterator(index))) }
    override fun removeAt(index: Int): T = asAccess { it.removeAt(index) }
    override fun set(index: Int, element: T): T = asAccess { it.set(index, element) }
    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> = asAccess {
        IsoMutableList(fork(it.subList(fromIndex, toIndex)))
    }

    private inline fun <R> asAccess(crossinline block: (MutableList<T>) -> R): R = access { block(it as MutableList<T>) }
}

class IsoMutableListIterator<T> internal constructor(stateHolder: StateHolder<MutableListIterator<T>>) :
    IsolateState<MutableListIterator<T>>(stateHolder), MutableListIterator<T> {
    override fun hasPrevious(): Boolean = access { it.hasPrevious() }
    override fun nextIndex(): Int = access { it.nextIndex() }
    override fun previous(): T = access { it.previous() }
    override fun previousIndex(): Int = access { it.previousIndex() }
    override fun add(element: T) = access { it.add(element) }
    override fun hasNext(): Boolean = access { it.hasNext() }
    override fun next(): T = access { it.next() }
    override fun set(element: T) = access { it.set(element) }
    override fun remove() = access { it.remove() }
}