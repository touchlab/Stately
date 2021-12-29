package co.touchlab.stately.collections

import co.touchlab.stately.concurrency.Synchronizable
import co.touchlab.stately.concurrency.synchronize

class ConcurrentMutableSet<E> internal constructor(rootArg: Synchronizable?, private val del: MutableSet<E>) :
    ConcurrentMutableCollection<E>(rootArg, del),
    MutableSet<E> {
    constructor() : this(null, mutableSetOf())

    fun <R> block(f: (MutableSet<E>) -> R): R = syncTarget.synchronize {
        val wrapper = MutableSetWrapper(del)
        val result = f(wrapper)
        wrapper.set = mutableSetOf()
        result
    }
}

internal class MutableSetWrapper<E>(internal var set: MutableSet<E>) : MutableCollectionWrapper<E>(set), MutableSet<E>