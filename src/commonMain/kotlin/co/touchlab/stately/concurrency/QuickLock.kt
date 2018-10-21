package co.touchlab.stately.concurrency

/**
 * On JVM this is a java.util.concurrent.Semaphore instance. On anything from apple this is an NSLock. On
 * all other platforms this will currently be a spin lock, as pthread_mutex requires a destructor.
 */
expect class QuickLock():Lock{
    override fun lock()
    override fun unlock()
}

fun createQuickLock():QuickLock = QuickLock()