package co.touchlab.stately.collections

import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

actual class MPWorker actual constructor(){
    private val executor = Executors.newSingleThreadExecutor()
    actual fun <T> runBackground(backJob: () -> T): MPFuture<T> {
        return MPFuture(executor.submit(backJob) as Future<T>)
    }

    actual fun requestTermination() {
        executor.shutdown()
        executor.awaitTermination(30, TimeUnit.SECONDS)
    }
}

actual class MPFuture<T>(private val future:Future<T>) {
    actual fun consume():T = future.get()
}

actual fun sleep(time: Long) {
    Thread.sleep(time)
}