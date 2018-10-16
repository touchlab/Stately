package co.touchlab.stately.concurrency

import java.util.concurrent.atomic.AtomicInteger

actual class AtomicInt actual constructor(value_: Int) {
    private val atom = AtomicInteger(value_)
    actual var value: Int
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
}