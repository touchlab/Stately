package co.touchlab.stately.concurrency

/**
 * Multiplatform AtomicInt implementation
 */
actual class AtomicInt actual constructor(value_: Int) {
    actual var value: Int = value_

    actual fun increment() {
        value++
    }

    actual fun decrement() {
        value--
    }

    actual fun addAndGet(delta: Int): Int {
        value += delta
        return value
    }

    actual fun compareAndSet(expected: Int, new: Int): Boolean {
        return if (expected == value) {
            value = new
            true
        } else {
            false
        }
    }
}