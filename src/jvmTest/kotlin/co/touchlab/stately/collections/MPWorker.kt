package co.touchlab.stately.collections

import java.util.concurrent.Executors
import java.util.concurrent.Future

actual class MPWorker actual constructor(){
    private val executor = Executors.newSingleThreadExecutor()
    actual fun <T> runBackground(backJob: () -> T): MPFuture<T> {
        return MPFuture(executor.submit(backJob) as Future<T>)
    }

    actual fun requestTermination() {
        executor.shutdown()
    }
}

actual class MPFuture<T>(private val future:Future<T>) {
    actual fun consume():T = future.get()
}

actual fun sleep(time: Long) {
    Thread.sleep(time)
}