package co.touchlab.stately.concurrency

import kotlin.native.concurrent.AtomicInt

actual class Lock actual constructor() {
    val lock = AtomicInt(0)

    actual fun lock() {
        spinLock()
    }

    actual fun unlock() {
        spinUnlock()
    }

    actual fun tryLock():Boolean {
        return lock.compareAndSet(0, 1)
    }

    private fun spinLock(){
        while (!lock.compareAndSet(0, 1)){}
    }

    private fun spinUnlock(){
        if(!lock.compareAndSet(1, 0))
            throw IllegalStateException("Lock must always be 1 in unlock")
    }
}