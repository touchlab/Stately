package co.touchlab.stately.isolate

interface StateRunner {
    fun <R> stateRun(block: () -> R): R
    fun stop()
}

expect class BackgroundStateRunner constructor() : StateRunner {
    override fun <R> stateRun(block: () -> R): R
    override fun stop()
}
