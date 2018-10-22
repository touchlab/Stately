package co.touchlab.stately.collections

actual class MPWorker actual constructor() {
    actual fun <T> runBackground(backJob: () -> T): MPFuture<T> = MPFuture(backJob())
    actual fun requestTermination() {}
}

actual class MPFuture<T>(private val result:T) {
    actual fun consume(): T = result
}

actual fun sleep(time: Long) {}