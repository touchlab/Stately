package co.touchlab.stately.concurrency

/**
 * The pthread_mutex api requires a call to shut down the mutex. There are no destructors
 * in native, and unless we find a simple way to have similar functionality, or a different
 * mutex, we'll need a 'close' for native implementations.
 */
interface Mutex:Lock{
    fun close()
}