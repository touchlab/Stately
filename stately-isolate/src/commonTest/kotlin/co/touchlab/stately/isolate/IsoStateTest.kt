package co.touchlab.stately.isolate

import co.touchlab.stately.freeze
import co.touchlab.testhelp.concurrency.ThreadOperations
import co.touchlab.testhelp.isNative
import co.touchlab.testhelp.printStackTrace

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
                    l.add(SomeData("arst ${rcount}"))
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
    fun nativeStateHolderInitChecks() {
        if(isNative){

            assertFails {
                StateHolder(SomeData("arst").freeze(), defaultStateRunner)
            }

            assertFails {
                val someData = SomeData("arst")
                StateHolder(someData, defaultStateRunner)
                someData.freeze()
            }

            assertFails {
                val iso = IsolateState {SomeData("aaa")}
                val sd = iso.access { it }
                println("Shouldn't get here $sd")
            }

        }
    }

    @Test
    fun isolatedProducer() {
        if(isNative) {
            assertFails {
                val map = mutableMapOf<String, String>()
                createState ({ map }, defaultStateRunner)
            }
        }
    }

    @Test
    fun noLeakingState() {
        if(isNative) {
            val ls = LeakyState()
            assertFails {
                ls.leak()
            }
            Unit
        }
    }

    @Test
    fun throwExceptions(){
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

    class LeakyState: IsolateState<MutableList<String>>({ mutableListOf()}) {
        fun leak() {
            var l : MutableList<String>? = null
            access { l = it }
            l
        }
    }

    data class SomeData(val s: String)
}