package co.touchlab.stately.concurrency

actual open class Synchronizable(private val _lock: Lock) {
    actual constructor() : this(Lock())

    fun <R> runSynchronized(block: () -> R): R = _lock.withLock(block)
}

actual inline fun <R> Synchronizable.synchronize(noinline block: () -> R): R = runSynchronized(block)