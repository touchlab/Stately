package co.touchlab.stately.concurrency

import java.util.concurrent.Semaphore

actual class QuickLock : Lock {
    private val mutex = Semaphore(1)

    actual override fun lock() {
        mutex.acquire()
    }

    actual override fun unlock() {
        mutex.release()
    }
}