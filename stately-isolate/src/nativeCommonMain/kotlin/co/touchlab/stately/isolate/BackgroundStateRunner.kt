package co.touchlab.stately.isolate

import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker
import kotlin.native.concurrent.freeze

actual class BackgroundStateRunner : StateRunner {
    internal val stateWorker = Worker.start(errorReporting = false)

    actual override fun <R> stateRun(block: () -> R): R {
        val result = stateWorker.execute(
            TransferMode.SAFE, { block.freeze() },
            {
                try {
                    Ok(it()).freeze()
                } catch (e: Throwable) {
                    Thrown(e).freeze()
                }
            }
        ).result
        return when (result) {
            is Ok<*> -> result.result as R
            is Thrown -> throw result.throwable
        }
    }
}
