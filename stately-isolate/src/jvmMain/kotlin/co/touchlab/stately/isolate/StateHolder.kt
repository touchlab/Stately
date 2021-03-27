package co.touchlab.stately.isolate

import co.touchlab.stately.concurrency.ThreadRef
import java.util.concurrent.atomic.AtomicBoolean

actual class StateHolder<out T : Any> internal actual constructor(t: T, actual val stateRunner: StateRunner) {
    actual val myState: T = t

    private var _isDisposed: AtomicBoolean = AtomicBoolean(false)

    actual val isDisposed: Boolean
        get() = _isDisposed.get()

    actual fun dispose() {
        _isDisposed.set(true)
    }

    private val threadRef = ThreadRef()
    actual val myThread: Boolean
        get() = threadRef.same()
}

internal actual val defaultStateRunner: StateRunner = BackgroundStateRunner()
