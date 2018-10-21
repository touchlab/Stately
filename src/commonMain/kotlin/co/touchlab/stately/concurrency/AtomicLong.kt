package co.touchlab.stately.concurrency

expect class AtomicLong(value_: Long) {
    var value:Long
    fun increment()
    fun decrement()

    fun addAndGet(delta: Int): Long
    fun compareAndSet(expected: Long, new: Long): Boolean
}
