package co.touchlab.stately.concurrency

import kotlin.native.concurrent.AtomicInt

actual class QuickLock actual constructor() : Lock {
    val lock = AtomicInt(0)

    actual override fun lock() {
        spinLock()
    }

    actual override fun unlock() {
        spinUnlock()
    }

    private fun spinLock(){
        while (!lock.compareAndSet(0, 1)){}
    }

    private fun spinUnlock(){
        if(!lock.compareAndSet(1, 0))
            throw IllegalStateException("Lock must always be 1 in unlock")
    }
}