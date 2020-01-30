package co.touchlab.stately.isolate

import co.touchlab.stately.collections.IsoMutableMap
import co.touchlab.testhelp.concurrency.ThreadOperations
import kotlin.test.Test
import kotlin.test.assertFails

class IsoStateTest {
    @Test
    fun basicTest() {
        val map = IsoMutableMap<String, SomeData>()
        val ops = ThreadOperations {}
        repeat(100) { outer ->
            ops.exe {
                val lmap = mutableMapOf<String, SomeData>()
                val base = outer * 1_000
                repeat(1_000) {
                    val num = it + base
                    lmap.put("key $num", SomeData("val $num"))
                }

                map.putAll(lmap)
            }
        }

        ops.run(4)

        println("Added, total ${map.size}")
    }
}

data class SomeData(val s: String)
