package co.touchlab.stately.concurrency

/**
 * A simple mutex lock.
 */
interface Lock{
    fun lock()
    fun unlock()
}