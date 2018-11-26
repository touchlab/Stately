package co.touchlab.stately.concurrency

import platform.Foundation.NSLock

actual class SingleLock actual constructor() : Lock {
    val lock = NSLock()

    actual override fun lock() {
        lock.lock()
    }

    actual override fun unlock() {
        lock.unlock()
    }

    actual override fun tryAcquire(): Boolean = lock.tryLock()
}