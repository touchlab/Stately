package co.touchlab.stately.concurrency

import platform.Foundation.NSLock

actual class QuickLock actual constructor() : Lock {
    val lock = NSLock()

    actual override fun lock() {
        lock.lock()
    }

    actual override fun unlock() {
        lock.unlock()
    }
}