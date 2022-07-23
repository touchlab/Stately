package co.touchlab.stately.isolate

import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker
import kotlin.native.concurrent.freeze
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotNull

class GuardedStableRefTest {

    var _w: Worker? = null
    val w: Worker
        get() = _w!!

    lateinit var g: GuardedStableRef<MutData>

    @BeforeTest
    fun setUp() {
        _w = Worker.start()
        g = w.execute(TransferMode.SAFE, {}) {
            GuardedStableRef(MutData(1))
        }.result.freeze()
    }

    @AfterTest
    fun tearDown() {
        w.execute(TransferMode.SAFE, { g }) {
            if (!it.disposed.value) {
                it.dispose()
            }
        }
        w.requestTermination().result
    }

    @Test
    fun wrongThreadAccessFails() {
        assertFails { g.state }
    }

    @Test
    fun wrongThreadDisposeFails() {
        assertFails { g.dispose() }
    }

    @Test
    fun mutateStateInThread() {
        val newMut = w.execute(TransferMode.SAFE, { g.freeze() }) {
            val mut = it.state
            mut.c = 42

            mut.copy()
        }.result

        assertEquals(42, newMut.c)
    }

    @Test
    fun disposeInThread() {
        w.execute(TransferMode.SAFE, { g }) {
            it.dispose()
        }.result
    }

    @Test
    fun alreadyDisposedException() {
        val exceptionResult = w.execute(TransferMode.SAFE, { g }) {
            it.dispose()

            try {
                it.dispose()
            } catch (e: Exception) {
                return@execute e
            }
            return@execute null
        }.result

        assertNotNull(exceptionResult)
    }

    data class MutData(var c: Int)
}
