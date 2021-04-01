package co.touchlab.stately.isolate

actual class StateHolder<out T : Any> actual constructor(t: T, actual val stateRunner: StateRunner) {
    actual val myState: T = t

    private var _isDisposed: Boolean = false

    actual val isDisposed: Boolean
        get() = _isDisposed

    actual fun dispose() {
        if (!isDisposed) _isDisposed = true
    }

    actual val myThread: Boolean = true
}
