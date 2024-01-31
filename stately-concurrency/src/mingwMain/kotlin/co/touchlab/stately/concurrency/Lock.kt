package co.touchlab.stately.concurrency

import co.touchlab.stately.strict.maybeFreeze
import kotlinx.cinterop.Arena
import kotlinx.cinterop.alloc
import kotlinx.cinterop.ptr
import platform.posix.PTHREAD_MUTEX_RECURSIVE
import platform.posix.pthread_mutex_destroy
import platform.posix.pthread_mutex_init
import platform.posix.pthread_mutex_lock
import platform.posix.pthread_mutex_tVar
import platform.posix.pthread_mutex_trylock
import platform.posix.pthread_mutex_unlock
import platform.posix.pthread_mutexattr_destroy
import platform.posix.pthread_mutexattr_init
import platform.posix.pthread_mutexattr_settype
import platform.posix.pthread_mutexattr_tVar

/**
 * A simple lock.
 * Implementations of this class should be re-entrant.
 */
@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual class Lock actual constructor() {
    private val arena = Arena()
    private val attr = arena.alloc<pthread_mutexattr_tVar>()
    private val mutex = arena.alloc<pthread_mutex_tVar>()

    init {
        pthread_mutexattr_init(attr.ptr)
        pthread_mutexattr_settype(attr.ptr, PTHREAD_MUTEX_RECURSIVE.toInt())
        pthread_mutex_init(mutex.ptr, attr.ptr)
        maybeFreeze()
    }

    actual fun lock() {
        pthread_mutex_lock(mutex.ptr)
    }

    actual fun unlock() {
        pthread_mutex_unlock(mutex.ptr)
    }

    actual fun tryLock(): Boolean = pthread_mutex_trylock(mutex.ptr) == 0

    fun internalClose() {
        pthread_mutex_destroy(mutex.ptr)
        pthread_mutexattr_destroy(attr.ptr)
        arena.clear()
    }
}

actual inline fun Lock.close() {
    internalClose()
}
