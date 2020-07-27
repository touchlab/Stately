package co.touchlab.stately.isolate

import co.touchlab.stately.concurrency.ThreadRef

actual class StateHolder<out T : Any> internal actual constructor(t: T, actual val stateRunner: StateRunner) {
    actual val myState: T = t

    actual fun dispose() {
    }

    private val threadRef = ThreadRef()
    actual val myThread: Boolean
        get() = threadRef.same()
}

internal actual val defaultStateRunner: StateRunner = BackgroundStateRunner()
