package co.touchlab.stately

expect class Lock(){
    fun lock()
    fun unlock()
}

/**
 * For pthread_mutex, which we're not using yet, but will be.
 */
expect fun Lock.close()