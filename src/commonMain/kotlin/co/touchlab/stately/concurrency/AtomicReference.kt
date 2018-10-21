package co.touchlab.stately.concurrency

expect class AtomicReference<T>(value_: T) {
    var value:T

    fun compareAndSet(expected: T, new: T): Boolean
}
