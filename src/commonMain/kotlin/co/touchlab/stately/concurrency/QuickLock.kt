package co.touchlab.stately.concurrency

/**
 * On native this will probably be implemented by a spin lock, because of
 * difficulty using pthread_mutex without a destructor. Only suitable
 * for quick operations.
 *
 * JVM will be using standard JVM concurrency primitives.
 */
expect class QuickLock():Lock{
    override fun lock()
    override fun unlock()
}

fun createQuickLock():QuickLock = QuickLock()