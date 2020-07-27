package co.touchlab.stately.collections

import co.touchlab.stately.isolate.IsolateState
import co.touchlab.stately.isolate.StateHolder
import co.touchlab.stately.isolate.createState

open class IsoMutableCollection<T> internal constructor(stateHolder: StateHolder<MutableCollection<T>>) :
    IsolateState<MutableCollection<T>>(stateHolder), MutableCollection<T> {
    constructor(producer: () -> MutableCollection<T>) : this(createState(producer))

    override fun equals(other: Any?): Boolean {
        return access { it == other }
    }

    override fun hashCode(): Int {
        return access { it.hashCode() }
    }

    override val size: Int
        get() = access { it.size }

    override fun contains(element: T): Boolean = access { it.contains(element) }
    override fun containsAll(elements: Collection<T>): Boolean = access { it.containsAll(elements) }
    override fun isEmpty(): Boolean = access { it.isEmpty() }
    override fun add(element: T): Boolean = access { it.add(element) }
    override fun addAll(elements: Collection<T>): Boolean = access { it.addAll(elements) }
    override fun clear() = access { it.clear() }
    override fun iterator(): MutableIterator<T> = access { IsoMutableIterator(fork(it.iterator())) }
    override fun remove(element: T): Boolean = access { it.remove(element) }
    override fun removeAll(elements: Collection<T>): Boolean = access { it.removeAll(elements) }
    override fun retainAll(elements: Collection<T>): Boolean = access { it.retainAll(elements) }
}
