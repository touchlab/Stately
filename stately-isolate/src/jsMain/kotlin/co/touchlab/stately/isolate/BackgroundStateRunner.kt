package co.touchlab.stately.isolate

actual class BackgroundStateRunner : StateRunner {
    actual override fun <R> stateRun(block: () -> R): R = block()
}
