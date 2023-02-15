package co.touchlab.stately.concurrency

expect open class Synchronizable()

expect inline fun <R> Synchronizable.synchronize(noinline  block: () -> R): R