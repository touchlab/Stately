package co.touchlab.stately.concurrency

import kotlin.native.concurrent.freeze

public actual fun <T> freezeLazy(initializer: () -> T): Lazy<T> = AtomicFrozenLazy(initializer)

internal class AtomicFrozenLazy<T>(private val initializer: () -> T): Lazy<T>{
    override val value: T
        get() = lock.withLock {
            if(valAtomic.value == null){
                valAtomic.value = initializer().freeze()
            }
            valAtomic.value!!
        }

    override fun isInitialized(): Boolean = valAtomic.value != null

    private val valAtomic = AtomicReference<T?>(null)
    private val lock = Lock()

}