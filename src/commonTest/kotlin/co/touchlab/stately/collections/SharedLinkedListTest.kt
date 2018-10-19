package co.touchlab.stately.collections

import kotlin.test.*

class LinkedListTest{

    //    @Test
    fun testSpinLock(){

    }

    private fun checkList(ll: SharedLinkedList<ListData>, vararg strings: String): Boolean {
        for(i in 0 until strings.size){
            if(ll.get(i).s != strings[i])
                return false
        }

        return true
    }

    @Test
    fun add(){
        val ll = makeTen()

        assertEquals(10, ll.size)
    }

    @Test
    fun addIndex(){
        val ll = SharedLinkedList<ListData>()

        try {
            ll.add(1, ListData("Item 1"))
            fail("Should've failed")
        } catch (e: Exception) {
        }

        assertEquals(0, ll.size)

        ll.add(0, ListData("Item 0"))
        assertEquals(1, ll.size)

        for(i in 1 until 4){
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
    fun addAllIndex(){
        println("trace a")
        val ll = SharedLinkedList<ListData>()
        println("trace b")
        val elements = listOf(ListData("Item 0"), ListData("Item 1"))
        println("trace c")
        try {
            ll.addAll(1, elements)
            fail("Bad index")
        } catch (e: Exception) {
        }
        println("trace d")

        ll.addAll(0, elements)
        println("trace e")
        checkList(ll, "Item 0", "Item 1")
        println("trace f")
        ll.addAll(1, listOf(ListData("Item a"), ListData("Item b")))
        println("trace g")
        checkList(ll, "Item 0", "Item a", "Item b", "Item 1")
        println("trace h")
    }

    @Test
    fun addAll(){
        val ll = SharedLinkedList<ListData>()
        val elements = listOf(ListData("Item 0"), ListData("Item 1"))
        ll.addAll(elements)

        checkList(ll, "Item 0", "Item 1")

        ll.addAll(listOf(ListData("Item a"), ListData("Item b")))

        checkList(ll, "Item 0", "Item 1", "Item a", "Item b")
    }

    @Test
    fun clear()
    {
        val ll = makeTen()
        assertEquals(10, ll.size)
        ll.clear()
        assertEquals(0, ll.size)
    }

    @Test
    fun contains(){
        val ll = makeTen()

        assertTrue(ll.contains(ListData("Item 2")))
        assertTrue(ll.contains(ListData("Item 7")))
        assertFalse(ll.contains(ListData("Item 10")))
    }

    @Test
    fun containsAll(){
        val ll = makeTen()

        assertTrue(ll.containsAll(listOf(ListData("Item 2"), ListData("Item 5"), ListData("Item 0"), ListData("Item 9"))))
        assertFalse(ll.containsAll(listOf(ListData("Item 2"), ListData("Item 5"), ListData("Item 0"), ListData("Item 10"))))
    }

    @Test
    fun get(){
        val ll = makeTen()
        assertEquals(ListData("Item 2"), ll.get(2))
    }

    @Test
    fun indexOf(){
        val ll = makeTen()
        assertEquals(3, ll.indexOf(ListData("Item 3")))
        assertEquals(0, ll.indexOf(ListData("Item 0")))
        assertEquals(-1, ll.indexOf(ListData("Item 10")))
        ll.clear()
        assertEquals(-1, ll.indexOf(ListData("Item 3")))
    }

    @Test
    fun isEmpty(){
        val ll = makeTen()
        assertFalse(ll.isEmpty())
        ll.clear()
        assertTrue(ll.isEmpty())
    }

    @Test
    fun remove(){
        val ll = makeTen()

        assertTrue(ll.remove(ListData("Item 4")))
        assertTrue(ll.remove(ListData("Item 8")))
        assertFalse(ll.remove(ListData("Item 8")))
        assertFalse(ll.remove(ListData("Item 88")))

        assertEquals(8, ll.size)

        checkList(ll, "Item 0", "Item 1", "Item 2", "Item 3", "Item 5", "Item 6", "Item 7", "Item 9")
    }

    @Test
    fun removeAll(){
        val ll = makeTen()

        assertTrue(ll.removeAll(listOf(ListData("Item 4"), ListData("Item 8"))))
        assertFalse(ll.removeAll(listOf(ListData("Item 4"), ListData("Item 8"))))
        assertFalse(ll.removeAll(listOf(ListData("Item 5"), ListData("Item 10"))))

        assertEquals(7, ll.size)

        checkList(ll, "Item 0", "Item 1", "Item 2", "Item 3", "Item 6", "Item 7", "Item 9")
    }

    @Test
    fun removeAt(){
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
    fun set(){
        val ll = makeTen()
        assertEquals(ListData("Item 2"), ll.get(2))
        ll.set(2, ListData("Heyo"))
        assertEquals(ListData("Heyo"), ll.get(2))
    }

    @Test
    fun size(){
        val ll = makeTen()

        assertEquals(10, ll.size)
        for(i in 0 until 10){
            ll.removeAt(0)
            assertEquals(9 - i, ll.size)
        }
    }

    @Test
    fun internalNodeAt(){
        val ll = makeTen()

        assertEquals(ll.internalNodeAt(5).nodeValue.s, "Item 5")
        try {
            ll.internalNodeAt(10)
            fail("Should've been IllegalArgumentException")
        }catch (e:IllegalArgumentException){}

        val empty = SharedLinkedList<ListData>()
        try {
            empty.internalNodeAt(0)
            fail("Should've been IllegalArgumentException")
        } catch (e: IllegalArgumentException) {}
    }

    @Test
    fun nodeAdd(){
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
    fun nodeRemove(){
        val ll = makeTen()

        ll.internalNodeAt(5).remove()
        assertEquals(9, ll.size)

        ll.internalNodeAt(0).remove()
        assertEquals(8, ll.size)

        try {
            ll.internalNodeAt(8).remove()
            fail("Shouldn't have this many values")
        } catch (e: Exception) {

        }

        val node = ll.internalNodeAt(7)
        assertEquals("Item 9", node.nodeValue.s)

        node.remove()
        assertEquals(7, ll.size)

        assertEquals("Item 8", ll.internalNodeAt(6).nodeValue.s)

        var loopCount = 20
        while(ll.size > 0){
            ll.internalNodeAt(0).remove()
            if(loopCount-- == 0){
                throw IllegalStateException("Something went wrong. Give up.")
            }
        }

        ll.add(ListData("Asdf 0"))

        ll.internalNodeAt(0).add(ListData("Asdf -1"))
    }

    @Test
    fun mtNodeRemove(){
        val ll = SharedLinkedList<ListData>().mpfreeze()
        val nodeList = mutableListOf<AbstractSharedLinkedList.Node<ListData>>()
        for (i in 0 until 1000) {
            nodeList.add(ll.addNode(ListData("a $i")))
        }

        nodeList.mpfreeze()

        val ops = ThreadOps<ListData>()
        for(i in 0 until 1000){
            ops.exe { nodeList.get(i).remove() }
        }

        ops.run(8, ll)

        assertEquals(1, ll.size)
    }

    @Test
    fun multipleThreads(){
        val ll = SharedLinkedList<ListData>().mpfreeze()
        val workers = Array(5){ createWorker()}
        val futures = Array(workers.size){wcount ->
            val worker = workers[wcount]
            worker.runBackground {
                ll.add(ListData("${wcount} 1"))
                ll.add(ListData("${wcount} 2"))
                ll.add(ListData("${wcount} 3"))
            }
        }

        val futureSet = futures.toSet()
        var consumed = 0
        while (consumed < futureSet.size) {
            futureSet.forEach {
                it.consume()
                consumed++
            }
        }

        workers.forEach {
            it.requestTermination()
        }

        assertEquals(5*3, ll.size)
    }

    @Test
    fun testBasicThreads(){
        val workers = Array(12) { createWorker() }
        val ll = SharedLinkedList<TestData>().mpfreeze()

        var count = 0

        workers.forEach {
            val valCount = count++
            it.runBackground {
                for(i in 0 until 1000){
                    ll.add(TestData("c: $valCount, i: $i"))
                }

                var countDown = 100
                ll.nodeIterator().forEach {
                    countDown--
                    if(countDown >= 0 && countDown % 10 == 0)
                    {
                        println("Removing: worker: $valCount, countDown: $countDown")
                        it.remove()
                    }
                }
            }
        }

        workers.forEach { it.requestTermination() }

        assertEquals(11880, ll.size)
    }

    @Test
    fun testDualList(){
        val operations = ArrayList<(Int, SharedLinkedList<MapData>)->Unit>()
        for(i in 0 until 1000){
            operations.add { workerId, list -> list.add(MapData("Worker: $workerId, Index: $i"))}
        }

        operations.mpfreeze()

        val listA = SharedLinkedList<MapData>().mpfreeze()
        val listB = SharedLinkedList<MapData>().mpfreeze()

        val workers = Array(8){
            MPWorker()
        }

        var count = 0
        val insertFutures = ArrayList<MPFuture<Unit>>()

        workers.forEach {
            val workerCount = count++
            insertFutures.add(it.runBackground {
                operations.forEach {
                    it(workerCount, listA)
                }
            })
        }

        count = 0
        workers.forEach {
            val workerCount = count++
            insertFutures.add(it.runBackground {
                operations.forEach {
                    it(workerCount, listB)
                }
            })
        }

        insertFutures.forEach { it.consume() }

        assertEquals(listA.size, listB.size)

        val removeFutures = ArrayList<MPFuture<Unit>>()
        count = 0
        workers.forEach {
            val workerCount = count++
            removeFutures.add(it.runBackground {
                listA.nodeIterator().forEach {
                    if(it.nodeValue.s.startsWith("Worker: $workerCount"))
                        it.remove()
                }
                listB.nodeIterator().forEach {
                    if(it.nodeValue.s.startsWith("Worker: $workerCount"))
                        it.remove()
                }
            })
        }

        removeFutures.forEach { it.consume() }
        workers.forEach { it.requestTermination() }

        assertEquals(listA.size, 0)
        assertEquals(listB.size, 0)
//        println("Debug Out")
//        listA.forEach { println(it) }
//        println("Debug Out")
    }

    private fun makeTen(): SharedLinkedList<ListData> {
        val ll = SharedLinkedList<ListData>()
        for (i in 0 until 10) {
            ll.add(ListData("Item $i"))
        }
        return ll
    }
}

expect fun currentTimeMillis(): Long

data class ListData(val s:String)
data class TestData(val s:String)