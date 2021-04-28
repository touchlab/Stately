package co.touchlab.stately.isolate

import java.util.concurrent.Callable
import java.util.concurrent.Executors

actual class BackgroundStateRunner actual constructor() : StateRunner {
    private val stateExecutor = Executors.newSingleThreadExecutor { r ->
        Executors.defaultThreadFactory().newThread(r).also { it.isDaemon = true }
    }

    actual override fun <R> stateRun(block: () -> R): R {
        val result = stateExecutor.submit(
            Callable<RunResult> {
                try {
                    Ok(block())
                } catch (e: Throwable) {
                    Thrown(e)
                }
            }
        ).get()

        return when (result) {
            is Ok<*> -> result.result as R
            is Thrown -> throw result.throwable
        }
    }

    actual override fun stop() {
        stateExecutor.shutdown()
    }
}
