package co.touchlab.stately.concurrency

/**
 * Multiplatform AtomicInt implementation
 */
expect class AtomicInt(value_: Int) {
    var value:Int
    fun increment()
    fun decrement()

    fun addAndGet(delta: Int): Int
    fun compareAndSet(expected: Int, new: Int): Boolean
}
