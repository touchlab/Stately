package co.touchlab.stately.concurrency

import java.util.concurrent.atomic.AtomicLong

actual class AtomicLong actual constructor(value_: Long) {
    private val atom = AtomicLong(value_)
    actual var value: Long
        get() = atom.get()
        set(value) {
            atom.set(value)
        }

    actual fun increment() {
        atom.incrementAndGet()
    }
    actual fun decrement() {
        atom.decrementAndGet()
    }

    actual fun addAndGet(delta: Int): Long = atom.addAndGet(delta.toLong())
    actual fun compareAndSet(expected: Long, new: Long): Boolean = atom.compareAndSet(expected, new)
}