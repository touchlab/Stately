package co.touchlab.stately.isolate

actual class BackgroundStateRunner actual constructor() : StateRunner {
    actual override fun <R> stateRun(block: () -> R): R = block()
    actual override fun stop() {
    }
}
