package co.touchlab.stately.concurrency

/**
 * On JVM this is a java.util.concurrent.Semaphore instance. On anything from apple this is an NSLock. On
 * all other platforms this will currently be a spin lock, as pthread_mutex requires a destructor.
 */
actual class QuickLock actual constructor() : Lock {
    var locked = false
    actual override fun lock() {
        if(locked)
            throw IllegalStateException("Locks are non-reentrant and JS is single threaded")
        locked = true
    }
    actual override fun unlock() {
        locked = false
    }
}