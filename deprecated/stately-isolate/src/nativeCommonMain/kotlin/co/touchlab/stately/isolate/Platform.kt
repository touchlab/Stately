package co.touchlab.stately.isolate

import co.touchlab.stately.concurrency.GuardedStableRef
import co.touchlab.stately.concurrency.ThreadRef
import kotlin.native.concurrent.ensureNeverFrozen
import kotlin.native.concurrent.isFrozen

/**
 * Do not directly use this. You will have state issues. You can only interact with this class
 * from the state thread.
 */
actual class StateHolder<out T : Any> actual constructor(t: T, actual val stateRunner: StateRunner) {
    private val stableRef: GuardedStableRef<T>

    init {
        if (t.isFrozen) {
            throw IllegalStateException("Mutable state shouldn't be frozen")
        }
        t.ensureNeverFrozen()
        stableRef = GuardedStableRef(t)
    }

    actual val myState: T
        get() = stableRef.state

    actual fun dispose() {
        stableRef.dispose()
    }

    private val threadRef = ThreadRef()
    actual val myThread: Boolean
        get() = threadRef.same()

    actual val isDisposed: Boolean
        get() = stableRef.isDisposed
}
