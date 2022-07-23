/*
 * Copyright (C) 2018 Touchlab, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.touchlab.stately.collections

import co.touchlab.stately.concurrency.value
import co.touchlab.stately.freeze
import co.touchlab.testhelp.concurrency.ThreadOperations
import co.touchlab.testhelp.isNativeFrozen
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LinkedListTest {

    private fun checkList(ll: SharedLinkedList<ListData>, vararg strings: String): Boolean {
        for (i in 0 until strings.size) {
            if (ll.get(i).s != strings[i]) {
                return false
            }
        }

        return true
    }

    @Test
    fun testInitFrozen() {
        assertTrue(SharedLinkedList<ListData>().isNativeFrozen)
    }

    @Test
    fun add() {
        val ll = makeTen()

        assertEquals(10, ll.size)
    }

    @Test
    fun addIndex() {
        val ll = SharedLinkedList<ListData>()

        assertFails { ll.add(1, ListData("Item 1")) }

        assertEquals(0, ll.size)

        ll.add(0, ListData("Item 0"))
        assertEquals(1, ll.size)

        for (i in 1 until 4) {
            ll.add(i, ListData("Item $i"))
        }

        assertEquals(4, ll.size)

        checkList(ll, "Item 0", "Item 1", "Item 2", "Item 3")

        ll.add(0, ListData("Before"))

        checkList(ll, "Before", "Item 0", "Item 1", "Item 2", "Item 3")

        ll.add(2, ListData("Middle"))

        checkList(ll, "Before", "Item 0", "Middle", "Item 1", "Item 2", "Item 3")

        ll.clear()

        checkList(ll)

        ll.add(0, ListData("Asdf"))

        checkList(ll, "Asdf")
    }

    @Test
    fun addAllIndex() {
        val ll = SharedLinkedList<ListData>()
        val elements = listOf(ListData("Item 0"), ListData("Item 1"))

        assertFails { ll.addAll(1, elements) }

        ll.addAll(0, elements)
        checkList(ll, "Item 0", "Item 1")
        ll.addAll(1, listOf(ListData("Item a"), ListData("Item b")))
        checkList(ll, "Item 0", "Item a", "Item b", "Item 1")
    }

    @Test
    fun addAll() {
        val ll = SharedLinkedList<ListData>()
        val elements = listOf(ListData("Item 0"), ListData("Item 1"))
        ll.addAll(elements)

        checkList(ll, "Item 0", "Item 1")

        ll.addAll(listOf(ListData("Item a"), ListData("Item b")))

        checkList(ll, "Item 0", "Item 1", "Item a", "Item b")
    }

    @Test
    fun clear() {
        val ll = makeTen()
        assertEquals(10, ll.size)
        ll.clear()
        assertEquals(0, ll.size)
    }

    @Test
    fun contains() {
        val ll = makeTen()

        assertTrue(ll.contains(ListData("Item 2")))
        assertTrue(ll.contains(ListData("Item 7")))
        assertFalse(ll.contains(ListData("Item 10")))
    }

    @Test
    fun containsAll() {
        val ll = makeTen()

        assertTrue(ll.containsAll(listOf(ListData("Item 2"), ListData("Item 5"), ListData("Item 0"), ListData("Item 9"))))
        assertFalse(ll.containsAll(listOf(ListData("Item 2"), ListData("Item 5"), ListData("Item 0"), ListData("Item 10"))))
    }

    @Test
    fun get() {
        val ll = makeTen()
        assertEquals(ListData("Item 2"), ll.get(2))
    }

    @Test
    fun indexOf() {
        val ll = makeTen()
        assertEquals(3, ll.indexOf(ListData("Item 3")))
        assertEquals(0, ll.indexOf(ListData("Item 0")))
        assertEquals(-1, ll.indexOf(ListData("Item 10")))
        ll.clear()
        assertEquals(-1, ll.indexOf(ListData("Item 3")))
    }

    @Test
    fun isEmpty() {
        val ll = makeTen()
        assertFalse(ll.isEmpty())
        ll.clear()
        assertTrue(ll.isEmpty())
    }

    @Test
    fun remove() {
        val ll = makeTen()

        assertTrue(ll.remove(ListData("Item 4")))
        assertTrue(ll.remove(ListData("Item 8")))
        assertFalse(ll.remove(ListData("Item 8")))
        assertFalse(ll.remove(ListData("Item 88")))

        assertEquals(8, ll.size)

        checkList(ll, "Item 0", "Item 1", "Item 2", "Item 3", "Item 5", "Item 6", "Item 7", "Item 9")
    }

    @Test
    fun removeAll() {
        val ll = makeTen()

        assertTrue(ll.removeAll(listOf(ListData("Item 4"), ListData("Item 8"))))
        assertFalse(ll.removeAll(listOf(ListData("Item 4"), ListData("Item 8"))))
        assertFalse(ll.removeAll(listOf(ListData("Item 5"), ListData("Item 10"))))

        assertEquals(7, ll.size)

        checkList(ll, "Item 0", "Item 1", "Item 2", "Item 3", "Item 6", "Item 7", "Item 9")
    }

    @Test
    fun removeAt() {
        val ll = makeTen()

        assertEquals(ll.removeAt(8), ListData("Item 8"))
        assertEquals(9, ll.size)
        assertEquals(ll.removeAt(4), ListData("Item 4"))
        assertEquals(8, ll.size)
        assertEquals(ll.removeAt(4), ListData("Item 5"))
        assertEquals(7, ll.size)

        checkList(ll, "Item 0", "Item 1", "Item 2", "Item 3", "Item 6", "Item 7", "Item 9")
    }

    @Test
    fun set() {
        val ll = makeTen()
        assertEquals(ListData("Item 2"), ll.get(2))
        ll.set(2, ListData("Heyo"))
        assertEquals(ListData("Heyo"), ll.get(2))
    }

    @Test
    fun size() {
        val ll = makeTen()

        assertEquals(10, ll.size)
        for (i in 0 until 10) {
            ll.removeAt(0)
            assertEquals(9 - i, ll.size)
        }
    }

    @Test
    fun internalNodeAt() {
        val ll = makeTen()

        assertEquals(ll.internalNodeAt(5).nodeValue.s, "Item 5")

        assertFails {
            ll.internalNodeAt(10)
        }

        val empty = SharedLinkedList<ListData>()

        assertFails {
            empty.internalNodeAt(0)
        }
    }

    @Test
    fun nodeAdd() {
        val ll = makeTen()

        ll.internalNodeAt(2).add(ListData("asdf"))
        assertEquals(ll.size, 11)
        assertEquals(ll.internalNodeAt(2).nodeValue.s, "asdf")
        assertEquals(ll.internalNodeAt(3).nodeValue.s, "Item 2")

        ll.internalNodeAt(0).add(ListData("a"))
        ll.internalNodeAt(0).add(ListData("b"))

        assertEquals(ll.size, 13)
        assertEquals(ll.internalNodeAt(0).nodeValue.s, "b")
        assertEquals(ll.internalNodeAt(1).nodeValue.s, "a")

        ll.internalNodeAt(12).add(ListData("c"))
        ll.internalNodeAt(13).add(ListData("d"))

        assertEquals(ll.size, 15)
        assertEquals(ll.internalNodeAt(12).nodeValue.s, "c")
        assertEquals(ll.internalNodeAt(13).nodeValue.s, "d")
    }

    @Test
    fun nodeRemove() {
        val ll = makeTen()

        ll.internalNodeAt(5).remove()
        assertEquals(9, ll.size)

        ll.internalNodeAt(0).remove()
        assertEquals(8, ll.size)

        assertFails {
            ll.internalNodeAt(8).remove()
        }

        val node = ll.internalNodeAt(7)
        assertEquals("Item 9", node.nodeValue.s)

        node.remove()
        assertEquals(7, ll.size)

        assertEquals("Item 8", ll.internalNodeAt(6).nodeValue.s)

        var loopCount = 20
        while (ll.size > 0) {
            ll.internalNodeAt(0).remove()
            if (loopCount-- == 0) {
                throw IllegalStateException("Something went wrong. Give up.")
            }
        }

        ll.add(ListData("Asdf 0"))

        ll.internalNodeAt(0).add(ListData("Asdf -1"))
    }

    @Test
    fun nodeRemovePermanent() {
        val ll = makeTen()
        val node = ll.internalNodeAt(5)
        node.remove(permanent = false)
        assertEquals(9, ll.size)
        assertFalse(node.isRemoved)

        val node2 = ll.internalNodeAt(5)
        node2.remove(permanent = true)
        assertEquals(8, ll.size)
        assertTrue(node2.isRemoved)
    }

    @Test
    fun mtNodeAdd() {
        val LOOPS = 100
        val DOOPS = 100
        val ll = SharedLinkedList<ListData>().freeze()
        val nodeList = mutableListOf<AbstractSharedLinkedList.Node<ListData>>()
        for (i in 0 until LOOPS) {
            nodeList.add(ll.addNode(ListData("a $i")))
        }

        nodeList.freeze()

        val ops = ThreadOperations { }
        for (i in 0 until LOOPS) {
            ops.exe {
                val node = nodeList.get(i)
                for (j in 0 until DOOPS) {
                    node.add(ListData("a $i sub $j"))
                }
                node.remove()
            }
        }

        ops.run(8)

        assertEquals(DOOPS * LOOPS, ll.size)

        var loopCount = 0
        var doopCount = 0

    /*var debugPrint = 0
    println("total size ${ll.size}")
    var handCount = 0
    ll.forEach { handCount++ }

    println("hand size ${handCount}")

    ll.forEach {
        if(debugPrint < 2500)
        println(it)
        debugPrint++
    }*/

        ll.iterator().forEach {
            assertEquals(ListData("a $loopCount sub $doopCount"), it)

            doopCount++
            if (doopCount == DOOPS) {
                doopCount = 0
                loopCount++
            }
        }
    }

    /**
     * Test multiple threads removing and occasionally adding nodes concurrently.
     */
    @Test
    fun mtNodeRemove() {
        val LOOPS = 2_000
        val ll = SharedLinkedList<ListData>().freeze()
        val nodeList = mutableListOf<AbstractSharedLinkedList.Node<ListData>>()
        for (i in 0 until LOOPS) {
            nodeList.add(ll.addNode(ListData("a $i")))
        }

        nodeList.freeze()

        val ops = ThreadOperations { }
        for (i in 0 until LOOPS) {
            ops.exe {
                val node = nodeList.get(i)
                if (i % 100 == 0) {
                    node.add(ListData("Filler $i"))
                }
                node.remove()
            }
        }

        ops.run(8)

        assertEquals(LOOPS / 100, ll.size)
    }

    /**
     * Tests that set is stable on nodes in multiple threads. Less critical
     * now that we update values in place (used to replace the node).
     */
    @Test
    fun mtNodeSet() {
        val LOOPS = 80
        val ll = SharedLinkedList<ListData>().freeze()
        val nodeList = mutableListOf<AbstractSharedLinkedList.Node<ListData>>()
        for (i in 0 until LOOPS) {
            nodeList.add(ll.addNode(ListData("a $i")))
        }

        nodeList.freeze()

        val ops = ThreadOperations { }
        for (i in 0 until LOOPS) {
            ops.exe { nodeList.get(i).set(ListData("b $i")) }
            ops.test { assertEquals(ll.get(i), ListData("b $i")) }
        }

        ops.run(8, true)

        assertEquals(LOOPS, ll.size)
    }

    /**
     * Multithreaded add.
     */
    @Test
    fun mtAdd() {
        val ops = ThreadOperations { SharedLinkedList<ListData>() }

        val LOOPS = 50

        for (wcount in 0 until LOOPS) {
            ops.exe { ll ->
                ll.add(ListData("$wcount 1"))
                ll.add(ListData("$wcount 2"))
                ll.add(ListData("$wcount 3"))
            }
        }

        val ll = ops.run(threads = 5, randomize = true)

        assertEquals(LOOPS * 3, ll.size)
    }

    /**
     * Basic threading test.
     */
    @Test
    fun testBasicThreads() {
        val LOOPS = 2500
        val ops = ThreadOperations { SharedLinkedList<TestData>() }
        val ll = SharedLinkedList<TestData>().freeze()

        for (i in 0 until LOOPS) {
            ops.exe { ll.add(TestData("Value: $i")) }
            ops.test { ll.contains(TestData("Value: $i")) }
        }

        ops.run(30, true)

/*        val workers = Array(8) { createWorker() }

        var count = 0

        workers.forEach {
            val valCount = count++
            it.runBackground {
                for(i in 0 until 1000){
                    ll.add(TestData("c: $valCount, i: $i"))
                }

                //TODO: Figure out some threaded stress tests. The following
                //was acting as intended but failing when trying to remove the same node

                *//*val countDownEnd = 100 * valCount
                var countDownStart = countDownEnd +

                ll.nodeIterator().forEach {
                    countDown--
                    if(countDown >= 0 && countDown % 10 == 0)
                    {
                        try {

                            it.remove()
                        } catch (e: Exception) {
                            collisionCount.increment()
                        }
                    }
                }*//*
            }
        }

        workers.forEach { it.requestTermination() }*/

        assertEquals(LOOPS, ll.size)
    }

    @Test
    fun concurrentModificationExceptionIterator() {
        concurRun(false) {
            it.size
        }
        concurRun(false) {
            it.indexOf(MapData("val 5"))
        }

        concurRun(true) {
            it.removeAt(7)
        }
        concurRun(true) {
            it.add(MapData("arst"))
        }
        concurRun(true) {
            it.set(8, MapData("arst"))
        }
    }

    private fun concurRun(shouldFail: Boolean, block: (ll: SharedLinkedList<MapData>) -> Unit) {
        val ll = SharedLinkedList<MapData>()
        for (i in 0 until 10) {
            ll.add(MapData("val $i"))
        }

        val nodeIter = ll.nodeIterator()
        block(ll)
        if (shouldFail) {
            assertFails { nodeIter.next() }
        } else {
            assertEquals(nodeIter.next().nodeValue.s, "val 0")
        }

        val iter = ll.iterator()
        block(ll)
        if (shouldFail) {
            assertFails { iter.next() }
        } else {
            assertEquals(iter.next().s, "val 0")
        }
    }

    /**
     * Removed nodes should fail on any mutation related operations or being re-added
     */
    @Test
    fun testRemovedNode() {
        val ll = SharedLinkedList<ListData>().freeze()
        val node = ll.addNode(ListData("hey 22"))

        node.remove()

        assertFails { node.remove() }
        assertFails { node.readd() }
        assertFails { node.add(ListData("asdf")) }
        assertFails { node.set(ListData("rre")) }
        assertFails { ll.internalAdd(node) }
    }

    /**
     * Unlikely we'll get to max int edits on a list, but not exactly impossible either.
     * The version is only used for iterator and concurrent mutability issues
     */
    @Test
    fun versionRollover() {
        val ll = makeTen()
        assertTrue(ll.version.value > 0)
        ll.version.value = Int.MAX_VALUE - 4
        for (i in 0 until 4) {
            ll.add(ListData("a $i"))
        }
        assertEquals(0, ll.version.value)
    }

    @Test
    fun defaultZeroPool() {
        assertEquals(SharedLinkedList<ListData>().nodePool.pool.size, 0)
        assertEquals(CopyOnIterateLinkedList<ListData>().nodePool.pool.size, 0)
    }

    private fun makeTen(): SharedLinkedList<ListData> {
        val ll = SharedLinkedList<ListData>()
        for (i in 0 until 10) {
            ll.add(ListData("Item $i"))
        }
        return ll
    }
}

data class ListData(val s: String)
data class TestData(val s: String)
