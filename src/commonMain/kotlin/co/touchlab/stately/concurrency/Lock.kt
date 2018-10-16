package co.touchlab.stately.concurrency

/**
 * A simple lock that, unlike pthread_mutex, does not require releasing resources explicitly.
 * On iOS can be implemented with NSLock, but on other platforms, we would currently only
 * support a spin lock.
 */
interface Lock{
    fun lock()
    fun unlock()
}