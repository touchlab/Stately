package co.touchlab.stately.collections

import co.touchlab.testhelp.concurrency.ThreadOperations
import co.touchlab.testhelp.concurrency.sleep
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExtendingCollections {
    @Test
    fun atomicAdd() {
        fun IsoMutableMap<String, SomeData>.sizeAdd(sd: SomeData) = access {
            val s = size
            sleep(200)
            put("key $s", sd)
        }

        val map = IsoMutableMap<String, SomeData>()
        val ops = ThreadOperations {}
        repeat(20) { i ->
            ops.exe {
                map.sizeAdd(SomeData("val $i"))
            }
            ops.test {
                assertTrue(map.containsKey("key $i"))
            }
        }

        ops.run(4, true)

        assertEquals(20, map.size)
    }
}