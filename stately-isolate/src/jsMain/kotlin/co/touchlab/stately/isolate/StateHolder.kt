package co.touchlab.stately.isolate

actual class StateHolder<out T : Any> internal actual constructor(t: T, actual val stateRunner: StateRunner) {
    actual val myState: T = t

    actual fun dispose() {
    }

    actual val myThread: Boolean = true
}

internal actual val defaultStateRunner: StateRunner = BackgroundStateRunner()
