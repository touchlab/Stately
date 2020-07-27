package co.touchlab.stately.collections

import co.touchlab.stately.isolate.IsolateState
import co.touchlab.stately.isolate.StateHolder

class IsoMutableIterator<T> internal constructor(stateHolder: StateHolder<MutableIterator<T>>) :
    IsolateState<MutableIterator<T>>(stateHolder), MutableIterator<T> {
    override fun hasNext(): Boolean = access { it.hasNext() }
    override fun next(): T = access { it.next() }
    override fun remove() = access { it.remove() }
}
