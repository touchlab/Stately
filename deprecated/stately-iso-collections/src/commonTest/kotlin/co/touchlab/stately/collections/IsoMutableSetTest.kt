package co.touchlab.stately.collections

import co.touchlab.testhelp.concurrency.ThreadOperations
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsoMutableSetTest : IsoMutableCollectionTest() {
    override fun defaultCollection(): IsoMutableCollection<SomeData> = IsoMutableSet()

    @Test
    fun addAll() {
        val set = addABunch()
        assertEquals(set.size, 500)
        set.addAll(listOf(SomeData("a"), SomeData("b"), SomeData("a")))
        assertEquals(set.size, 502)
    }

    @Test
    fun equalsTest() {
        val set = addABunch()
        val set2 = addABunch()
        assertTrue(set.equals(set2))
    }
}

abstract class IsoMutableCollectionTest {

    @Test
    fun stress() {
        val set = defaultCollection()

        val ops = ThreadOperations {}
        val times = 30_000
        repeat(times) { i ->
            ops.exe { set.add(SomeData("key $i")) }
            ops.test { assertTrue(set.contains(SomeData("key $i"))) }
        }

        ops.run(4)

        assertEquals(set.size, times)
    }

    @Test
    fun contains() {
        val set = addABunch()
        repeat(500) { i ->
            assertTrue(set.contains(SomeData("key $i")))
        }
    }

    @Test
    fun containsAll() {
        val set = addABunch()

        val checkList = mutableListOf<SomeData>()
        repeat(500) { i ->
            if (Random.nextDouble() > .8) {
                checkList.add(SomeData("key $i"))
            }
        }

        // In theory, this will fail occasionally, but pretty rare
        assertTrue(checkList.size > 0)

        assertTrue(set.containsAll(checkList))
    }

    @Test
    fun isEmpty() {
        val set = defaultCollection()
        assertTrue(set.isEmpty())
        addABunch(set)
        assertFalse(set.isEmpty())
    }

    @Test
    fun add() {
        contains() // Contains really covers this
    }

    @Test
    fun clear() {
        val set = addABunch()
        assertEquals(set.size, 500)
        set.clear()
        assertEquals(set.size, 0)
    }

    @Test
    fun iterator() {
        val set = addABunch()
        val iterator = set.iterator()
        var count = 0
        iterator.forEach { count++ }
        assertEquals(500, count)
    }

    @Test
    fun remove() {
        val set = addABunch()

        assertTrue(set.remove(SomeData("key 55")))
        assertTrue(set.remove(SomeData("key 155")))
        assertFalse(set.remove(SomeData("key 155")))
        assertFalse(set.remove(SomeData("key2 255")))

        assertEquals(498, set.size)
    }

    @Test
    fun removeAll() {
        val set = addABunch()

        assertTrue(
            set.removeAll(
                listOf(
                    SomeData("key 55"),
                    SomeData("key 155"),
                    SomeData("key2 65")
                )
            )
        )

        assertEquals(498, set.size)

        assertTrue(
            set.removeAll(
                listOf(
                    SomeData("key 65"),
                    SomeData("key 165")
                )
            )
        )

        assertEquals(496, set.size)

        assertFalse(
            set.removeAll(
                listOf(
                    SomeData("key 65"),
                    SomeData("key 165")
                )
            )
        )

        assertEquals(496, set.size)
    }

    @Test
    fun retainAll() {
        val set = addABunch()

        assertTrue(
            set.retainAll(
                listOf(
                    SomeData("key 55"),
                    SomeData("key 155"),
                    SomeData("key2 65")
                )
            )
        )

        assertEquals(2, set.size)

        assertFalse(
            set.retainAll(
                listOf(
                    SomeData("key 55"),
                    SomeData("key 155"),
                    SomeData("key2 65")
                )
            )
        )

        assertEquals(2, set.size)
    }

    abstract fun defaultCollection(): IsoMutableCollection<SomeData>

    internal fun addABunch(set: IsoMutableCollection<SomeData> = defaultCollection(), threads: Int = 4): IsoMutableCollection<SomeData> {
        val ops = ThreadOperations {}
        repeat(500) { i ->
            ops.exe { set.add(SomeData("key $i")) }
        }

        ops.run(threads)

        return set
    }
}
