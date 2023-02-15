package co.touchlab.stately.collections

import co.touchlab.stately.isolate.StateHolder
import co.touchlab.stately.isolate.StateRunner
import co.touchlab.stately.isolate.createState

open class IsoMutableSet<T> internal constructor(stateHolder: StateHolder<MutableSet<T>>) :
    IsoMutableCollection<T>(stateHolder), MutableSet<T> {
    constructor(stateRunner: StateRunner? = null, producer: () -> MutableSet<T> = { mutableSetOf() }) : this(createState(stateRunner, producer))
}
