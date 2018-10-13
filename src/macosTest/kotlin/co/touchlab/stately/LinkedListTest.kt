package co.touchlab.stately

import kotlin.native.concurrent.*
import kotlin.test.*

class LinkedListTest{
    @Test
    fun add(){
        val ll = makeTen()

        assertEquals(10, ll.size())
    }

    @Test
    fun remove(){
        val ll = makeTen()

        assertTrue(ll.remove(ListData("Item 4")))
        assertTrue(ll.remove(ListData("Item 8")))
        assertFalse(ll.remove(ListData("Item 8")))
        assertFalse(ll.remove(ListData("Item 88")))

        assertEquals(8, ll.size())
    }

    @Test
    fun nodeAt(){
        val ll = makeTen()

        assertEquals(ll.nodeAt(5).nodeValue.s, "Item 5")
        try {
            ll.nodeAt(10)
            fail("Should've been IllegalArgumentException")
        }catch (e:IllegalArgumentException){}

        val empty = DoublyLinkedList<ListData>()
        try {
            empty.nodeAt(0)
            fail("Should've been IllegalArgumentException")
        } catch (e: IllegalArgumentException) {}
    }

    @Test
    fun nodeAdd(){
        val ll = makeTen()

        ll.nodeAt(2).add(ListData("asdf"))
        assertEquals(ll.size(), 11)
        assertEquals(ll.nodeAt(2).nodeValue.s, "asdf")
        assertEquals(ll.nodeAt(3).nodeValue.s, "Item 2")

        ll.nodeAt(0).add(ListData("a"))
        ll.nodeAt(0).add(ListData("b"))

        assertEquals(ll.size(), 13)
        assertEquals(ll.nodeAt(0).nodeValue.s, "b")
        assertEquals(ll.nodeAt(1).nodeValue.s, "a")

        ll.nodeAt(12).add(ListData("c"))
        ll.nodeAt(13).add(ListData("d"))

        println(ll.debugPrint())

        assertEquals(ll.size(), 15)
        assertEquals(ll.nodeAt(12).nodeValue.s, "c")
        assertEquals(ll.nodeAt(13).nodeValue.s, "d")
    }

    @Test
    fun nodeRemove(){
        val ll = makeTen()

        ll.nodeAt(5).remove()
        assertEquals(9, ll.size())

        ll.nodeAt(0).remove()
        assertEquals(8, ll.size())

        try {
            ll.nodeAt(8).remove()
            fail("Shouldn't have this many values")
        } catch (e: Exception) {

        }

        val node = ll.nodeAt(7)
        assertEquals("Item 9", node.nodeValue.s)

        node.remove()
        assertEquals(7, ll.size())

        assertEquals("Item 8", ll.nodeAt(6).nodeValue.s)

        var loopCount = 20
        while(ll.size() > 0){
            println(ll.debugPrint())
            ll.nodeAt(0).remove()
            if(loopCount-- == 0){
                throw IllegalStateException("Something went wrong. Give up.")
            }
        }

        ll.add(ListData("Asdf 0"))

        ll.nodeAt(0).add(ListData("Asdf -1"))

        println(ll.debugPrint())
    }

//    @Test
    fun multipleThreads(){
        val ll = DoublyLinkedList<ListData>().freeze()
        val workers = Array(5){Worker.start()}
        val futures = Array(workers.size){
            val worker = workers[it]
            worker.execute(TransferMode.SAFE, {Pair(it, ll)}){
                it.second.add(ListData("${it.first} 1"))
                it.second.add(ListData("${it.first} 2"))
                it.second.add(ListData("${it.first} 1"))
            }
        }

        val futureSet = futures.toSet()
        var consumed = 0
        while (consumed < futureSet.size) {
            val ready = futureSet.waitForMultipleFutures(10000)
            ready.forEach {
                it.consume { result ->
                    consumed++
                }
            }
        }

        workers.forEach {
            it.requestTermination().result
        }

        ll.toList().forEach {
            println(it)
        }

        assertEquals(5*2, ll.size())
    }

    private fun makeTen(): DoublyLinkedList<ListData> {
        val ll = DoublyLinkedList<ListData>()
        for (i in 0 until 10) {
            ll.add(ListData("Item $i"))
        }
        return ll
    }
}

data class ListData(val s:String)