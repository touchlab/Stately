package co.touchlab.stately.concurrency

actual typealias Synchronizable = Any

actual inline fun <R> Synchronizable.synchronize(noinline block: () -> R): R = synchronized(this, block)