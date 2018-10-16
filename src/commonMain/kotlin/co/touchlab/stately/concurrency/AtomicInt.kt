package co.touchlab.stately.concurrency

expect class AtomicInt(value_: Int) {
    var value:Int
    fun increment()
    fun decrement()
}
