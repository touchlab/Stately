package co.touchlab.stately.collections

import co.touchlab.stately.isolate.StateHolder
import co.touchlab.stately.isolate.StateRunner
import co.touchlab.stately.isolate.createState

@ExperimentalStdlibApi
open class IsoArrayDeque<E>
internal constructor(stateHolder: StateHolder<ArrayDeque<E>>) :
    IsoMutableList<E>(stateHolder) {
    constructor(stateRunner: StateRunner? = null, producer: () -> ArrayDeque<E> = { ArrayDeque() }) : this(createState(stateRunner, producer))

    /**
     * Returns the first element, or throws [NoSuchElementException] if this deque is empty.
     */
    public fun first(): E = asAccess { it.first() }

    /**
     * Returns the first element, or `null` if this deque is empty.
     */
    public fun firstOrNull(): E? = asAccess { it.firstOrNull() }

    /**
     * Returns the last element, or throws [NoSuchElementException] if this deque is empty.
     */
    public fun last(): E = asAccess { it.last() }

    /**
     * Returns the last element, or `null` if this deque is empty.
     */
    public fun lastOrNull(): E? = asAccess { it.lastOrNull() }

    /**
     * Prepends the specified [element] to this deque.
     */
    public fun addFirst(element: E) = asAccess { it.addFirst(element) }

    /**
     * Appends the specified [element] to this deque.
     */
    public fun addLast(element: E) = asAccess { it.addLast(element) }

    /**
     * Removes the first element from this deque and returns that removed element, or throws [NoSuchElementException] if this deque is empty.
     */
    public fun removeFirst(): E = asAccess { it.removeFirst() }

    /**
     * Removes the first element from this deque and returns that removed element, or returns `null` if this deque is empty.
     */
    public fun removeFirstOrNull(): E? = asAccess { it.removeFirstOrNull() }

    /**
     * Removes the last element from this deque and returns that removed element, or throws [NoSuchElementException] if this deque is empty.
     */
    public fun removeLast(): E = asAccess { it.removeLast() }

    /**
     * Removes the last element from this deque and returns that removed element, or returns `null` if this deque is empty.
     */
    public fun removeLastOrNull(): E? = asAccess { it.removeLastOrNull() }

    private inline fun <R> asAccess(crossinline block: (ArrayDeque<E>) -> R): R =
        access { block(it as ArrayDeque<E>) }
}
