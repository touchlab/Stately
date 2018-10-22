package co.touchlab.stately.concurrency

/**
 * Multiplatform AtomicLong implementation
 */
actual class AtomicLong actual constructor(value_: Long) {
    actual var value: Long = value_

    actual fun increment() {
        value++
    }

    actual fun decrement() {
        value--
    }

    actual fun addAndGet(delta: Int): Long {
        value += delta
        return value
    }

    actual fun compareAndSet(expected: Long, new: Long): Boolean {
        return if (expected == value) {
            value = new
            true
        } else {
            false
        }
    }
}