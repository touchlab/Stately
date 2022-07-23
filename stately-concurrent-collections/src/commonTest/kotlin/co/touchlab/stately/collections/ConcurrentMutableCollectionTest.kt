package co.touchlab.stately.collections

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

class ConcurrentMutableCollectionTest {
    @Test
    @NoJsTest
    fun add() {
        runBoth<SomeData>(block = { coll, count ->
            coll.add(SomeData("count $count"))
        }) {
            assertEquals(it.size, DEFAULT_RUNS * 2)
        }
    }

    @Test
    @NoJsTest
    fun contains() {
        runBoth<SomeData>(runs = 1_000, block = { coll, count ->
            coll.add(SomeData("count $count"))
        }) { coll ->
            repeat(2_000) { count ->
                coll.contains(SomeData("count $count"))
            }
        }
    }

    @Test
    @NoJsTest
    fun containsAll() {
        val checkList = ConcurrentMutableList<SomeData>()
        runBoth<SomeData>(runs = 1_000, block = { coll, count ->
            val someData = SomeData("count $count")
            coll.add(someData)
            checkList.add(someData)
        }) { coll ->
            coll.containsAll(checkList)
        }
    }

    @Test
    @NoJsTest
    fun blockCollection() {
        runBoth<SomeData>(runs = 100, block = { coll, count ->
            coll.blockCollection { mcoll ->
                repeat(20) { innerCount ->
                    val someData = SomeData("outer $count, inner $innerCount")
                    mcoll.add(someData)
                }
            }
        }) { coll ->
            assertEquals(4000, coll.blockCollection { it.size })
        }
    }

    @Test
    @NoJsTest
    fun blockCollectionLeak() {
        runBoth<SomeData>(runs = 100, block = { coll, count ->
            coll.blockCollection { mcoll ->
                repeat(20) { innerCount ->
                    val someData = SomeData("outer $count, inner $innerCount")
                    mcoll.add(someData)
                }
            }
        }) { coll ->
            var holdLeak: MutableCollection<SomeData>? = null
            coll.blockCollection { holdLeak = it }
            assertFails { holdLeak!!.size }
        }
    }

    @Test
    @NoJsTest
    fun remove() {
        val checkList = ConcurrentMutableSet<SomeData>()
        runBoth<SomeData>(runs = 1_000, block = { coll, count ->
            val someData = SomeData("count $count")
            coll.add(someData)
            if (count % 10 == 0) {
                checkList.add(someData)
            }
        }) { coll ->
            checkList.forEach { assertTrue(coll.remove(it)) }
            assertEquals(checkList.size, 200)
            assertEquals(coll.size, 1800)
        }
    }

    @Test
    @NoJsTest
    fun removeAll() {
        val checkList = ConcurrentMutableSet<SomeData>()
        runBoth<SomeData>(runs = 1_000, block = { coll, count ->
            val someData = SomeData("count $count")
            coll.add(someData)
            if (count % 10 == 0) {
                checkList.add(someData)
            }
        }) { coll ->
            assertTrue(coll.removeAll(checkList))
            assertEquals(checkList.size, 200)
            assertEquals(coll.size, 1800)
        }
    }

    @Test
    @NoJsTest
    fun retainAll() {
        val checkList = ConcurrentMutableSet<SomeData>()
        runBoth<SomeData>(runs = 1_000, block = { coll, count ->
            val someData = SomeData("count $count")
            coll.add(someData)
            if (count % 10 == 0) {
                checkList.add(someData)
            }
        }) { coll ->
            assertTrue(coll.retainAll(checkList))
            assertEquals(checkList.size, 200)
            assertEquals(coll.size, 200)
        }
    }
}

fun <E> runBoth(
    runs: Int = DEFAULT_RUNS,
    block: (ConcurrentMutableCollection<E>, Int) -> Unit,
    verify: (ConcurrentMutableCollection<E>) -> Unit
) {
    val set = ConcurrentMutableSet<E>()
    runAlot(runs) {
        block(set, it)
    }
    verify(set)
    val list = ConcurrentMutableList<E>()
    runAlot(runs) {
        block(list, it)
    }
    verify(list)
}