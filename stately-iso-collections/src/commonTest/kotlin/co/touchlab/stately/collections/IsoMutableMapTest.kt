package co.touchlab.stately.collections

import co.touchlab.testhelp.concurrency.ThreadOperations
import co.touchlab.testhelp.isNative
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class IsoMutableMapTest {
    //    @Test
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

    @Test
    fun containsKey() {
        val map = testMap()
        repeat(50) { i ->
            val r = Random.nextInt(500)
            assertTrue(map.containsKey("key $r"))
            assertFalse(map.containsKey("keya $r"))
        }
    }

    @Test
    fun containsValue() {
        val map = testMap()
        repeat(50) { i ->
            val r = Random.nextInt(500)
            assertTrue(map.containsValue(SomeData("val $r")))
            assertFalse(map.containsValue(SomeData("vala $r")))
        }
    }

    @Test
    fun get() {
        val map = testMap()
        repeat(50) { i ->
            val r = Random.nextInt(500)
            assertNotNull(map.get("key $r"))
            assertNull(map.get("keya $r"))
        }
    }

    @Test
    fun isEmpty() {
        val map = IsoMutableMap<String, SomeData>()
        assertTrue(map.isEmpty())
        testMap(map)
        assertFalse(map.isEmpty())
    }

//    @Test
    fun entries() {
        if (!isNative) {
            return
        }

        val map = testMap()
        val entries = map.entries
        assertEquals(500, map.size)
        println("b 1")
        val first = entries.first()
        println("b 2")
        entries.remove(first)
        println("b 3")
        assertEquals(499, map.size)
    }

    @Test
    fun keys() {
        val map = testMap()
        val keys = map.keys
        assertEquals(500, map.size)
        val r = mutableListOf<String>()
        keys.forEachIndexed { index, s ->
            if (index % 10 == 0) {
                r.add(s)
            }
        }

        r.forEach { keys.remove(it) }
        assertEquals(450, map.size)
    }

    @Test
    fun values() {
        val map = testMap()
        val values = map.values
        values.remove(values.first())
        assertEquals(499, map.size)
    }

    @Test
    fun clear() {
        val map = testMap()
        map.clear()
        assertEquals(0, map.size)
    }

    @Test
    fun put() {
        val map = testMap()
        assertEquals(500, map.size)
    }

    @Test
    fun putAll() {
        val map = testMap()
        assertEquals(500, map.size)

        map.putAll(
            mapOf(
                Pair("a", SomeData("a")),
                Pair("b", SomeData("b")),
                Pair("c", SomeData("c")),
                Pair("a", SomeData("a"))
            )
        )

        assertEquals(503, map.size)
    }

    @Test
    fun remove() {
        val map = testMap()
        assertEquals(500, map.size)
        map.remove("key 432")
        map.remove("key 232")
        map.remove("key2 232")
        assertEquals(498, map.size)
    }

    @Test
    fun equals() {
        val isomap = testMap()
        val map = mutableMapOf<String, SomeData>()
        repeat(500) { i ->
            map.put("key $i", SomeData("val $i"))
        }

        assertTrue(isomap.equals(map))
    }

    private fun testMap(map: IsoMutableMap<String, SomeData> = IsoMutableMap()): IsoMutableMap<String, SomeData> {
        repeat(500) { i ->
            map.put("key $i", SomeData("val $i"))
        }

        return map
    }
}

data class SomeData(val s: String)
