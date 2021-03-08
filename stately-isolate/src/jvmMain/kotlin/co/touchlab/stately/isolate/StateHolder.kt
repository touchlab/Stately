package co.touchlab.stately.isolate

import co.touchlab.stately.concurrency.ThreadRef

actual class StateHolder<out T : Any> internal actual constructor(t: T, actual val stateRunner: StateRunner) {
    actual val myState: T = t

    private var _isDisposed: Boolean = false

    actual val isDisposed: Boolean
        get() = _isDisposed

    actual fun dispose() {
        if (!isDisposed) _isDisposed = true
    }

    private val threadRef = ThreadRef()
    actual val myThread: Boolean
        get() = threadRef.same()
}

internal actual val defaultStateRunner: StateRunner = BackgroundStateRunner()
