package co.touchlab.stately.concurrency

import java.util.concurrent.atomic.AtomicReference

actual class AtomicReference<T> actual constructor(value_: T) {
    private val atom = AtomicReference(value_)
    actual var value: T
        get() = atom.get()
        set(value) {
            atom.set(value)
        }
}