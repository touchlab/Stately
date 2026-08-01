package co.touchlab.stately.concurrency

import kotlin.native.runtime.GC
import kotlin.native.runtime.NativeRuntimeApi
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(NativeRuntimeApi::class)
class SynchronizableTest {
    @Test
    fun closesLockWhenCollected() {
        val closeCount = AtomicInt(0)
        val resource = object : SynchronizationResource() {
            override fun close() {
                super.close()
                closeCount.incrementAndGet()
            }
        }

        useResource(resource)

        repeat(100) {
            GC.collect()
            if (closeCount.get() == 1) {
                GC.collect()
                assertEquals(1, closeCount.get())
                return
            }
        }

        assertEquals(1, closeCount.get())
    }

    private fun useResource(resource: SynchronizationResource) {
        Synchronizable(resource).synchronize {}
    }
}
