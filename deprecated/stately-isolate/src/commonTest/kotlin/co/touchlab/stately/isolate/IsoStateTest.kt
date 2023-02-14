package co.touchlab.stately.isolate

import co.touchlab.stately.concurrency.ThreadRef
import co.touchlab.stately.freeze
import co.touchlab.testhelp.concurrency.ThreadOperations
import co.touchlab.testhelp.concurrency.background
import co.touchlab.testhelp.isNative
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IsoStateTest {
    @Test
    fun basicTest() {
        val ops = ThreadOperations {}

        val isoList = IsolateState { mutableListOf<SomeData>() }
        repeat(100_000) { rcount ->
            ops.exe {
                isoList.access { l ->
                    l.add(SomeData("arst $rcount"))
                }
            }
        }

        ops.run(4)

        val lsize = isoList.access { l ->
            l.size
        }

        assertEquals(100_000, lsize)
    }



    @Test
    fun throwExceptions() {
        val iso = IsolateState { mutableListOf("a") }
        try {
            iso.access { throw IllegalStateException("arst") }
            fail("Shouldn't be here")
        } catch (e: Exception) {
            println("Exception: $e")
            e.printStackTrace()
            assertTrue(e is IllegalStateException && e.message == "arst")
        }
    }

    class LeakyState : IsolateState<MutableList<String>>(producer = { mutableListOf() }) {
        fun leak() {
            var l: MutableList<String>? = null
            access { l = it }
            l
        }
    }

    data class SomeData(val s: String)

    @Test
    fun testNonMainThread() {
        val bar = background {
            val foo = IsolateState { mutableListOf<String>() }
            val s = "arst"
            foo.access {
                it.add(s)
            }
            foo
        }

        assertEquals(bar.access { it.get(0) }, "arst")
    }

    @Test
    fun stateRunsOnSameThreadByDefault() {
        val first = IsolateState { mutableListOf<String>() }
        val second = IsolateState { mutableListOf<String>() }

        val firstThread = first.access { ThreadRef() }
        val isSame = second.access { firstThread.same() }

        assertTrue { isSame }
    }
}
