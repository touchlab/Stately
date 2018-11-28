package co.touchlab.stately.concurrency

import platform.posix.pthread_equal
import platform.posix.pthread_self
import kotlin.native.concurrent.AtomicReference
import kotlin.native.concurrent.freeze

actual class ThreadRef actual constructor(){
    private val threadRef = AtomicReference(pthread_self().freeze())
    actual fun reset() {
        threadRef.value = pthread_self().freeze()
    }
    actual fun same(): Boolean = pthread_equal(threadRef.value, pthread_self()) != 0
}