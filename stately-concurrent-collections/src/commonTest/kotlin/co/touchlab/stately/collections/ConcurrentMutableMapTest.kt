package co.touchlab.stately.collections

import co.touchlab.stately.concurrency.AtomicInt
import co.touchlab.testhelp.concurrency.sleep
import kotlinx.coroutines.delay
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConcurrentMutableMapTest {
    @Test
    @NoJsTest
    fun put() {
        val map = ConcurrentMutableMap<String, SomeData>()

        runAlot { run ->
            map.put("key $run", SomeData("value $run"))
        }

        assertEquals(map.size, 200_000)
    }

    @Test
    @NoJsTest
    fun contains() {
        val map = ConcurrentMutableMap<String, SomeData>()

        runAlot(runs = 10_000) { run ->
            map.put("key $run", SomeData("value $run"))
        }

        repeat(1000) { i ->
            val key = "key $i"
            assertTrue(map.containsKey(key), "Key not found '${key}'")
            val valueString = "value $i"
            assertTrue(map.containsValue(SomeData(valueString)), "Value not found '${valueString}'")
        }
    }

    @Test
    @NoJsTest
    fun remove() {
        val map = ConcurrentMutableMap<String, SomeData>()

        runAlot { run ->
            map.put("key $run", SomeData("value $run"))
        }

        assertEquals(map.size, DEFAULT_RUNS * 2)

        runAlot(runs = DEFAULT_RUNS / 10) { run ->
            map.remove("key ${run * 10}")
        }

        assertEquals(map.size, (DEFAULT_RUNS * 2) - ((DEFAULT_RUNS / 10) * 2))
    }

    @Test
    @NoJsTest
    fun block() {
        val map = ConcurrentMutableMap<String, SomeData>()

        runAlot(100) { run ->
            map.block { map ->
                repeat(1000) { innerRun ->
                    val item = (run * 1000) + innerRun
                    map.put("key $item", SomeData("value $item"))
                }
            }
        }

        assertEquals(map.size, DEFAULT_RUNS * 2)
    }

    @Test
    @NoJsTest
    fun computeIfAbsent() {
        val map = ConcurrentMutableMap<String, SomeData>()
        val count = AtomicInt(0)

        runAlot(1) { run ->
            map.computeIfAbsent("key") {
                sleep(1000)
                count.incrementAndGet()
                SomeData("value $run")
            }
        }

        assertEquals(count.get(), 1)
    }
}