package co.touchlab.stately.isolate

interface StateRunner {
    fun <R> stateRun(block: () -> R): R
}

expect class BackgroundStateRunner : StateRunner {
    override fun <R> stateRun(block: () -> R): R
}
