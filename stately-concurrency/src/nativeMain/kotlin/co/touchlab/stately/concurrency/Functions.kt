@file:Suppress("ktlint:standard:filename")

package co.touchlab.stately.concurrency

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

actual open class Synchronizable internal constructor(private val resource: SynchronizationResource) {
    actual constructor() : this(SynchronizationResource())

    @Suppress("unused")
    @OptIn(ExperimentalNativeApi::class)
    private val cleaner = createCleaner(resource, SynchronizationResource::close)

    fun <R> runSynchronized(block: () -> R): R = resource.lock.withLock(block)
}

internal open class SynchronizationResource(internal val lock: Lock = Lock()) {
    internal open fun close() {
        lock.close()
    }
}

actual inline fun <R> Synchronizable.synchronize(noinline block: () -> R): R = runSynchronized(block)
