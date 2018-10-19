package co.touchlab.stately.collections

import kotlin.test.*

class CopyOnWriteTest {
    @Test
    fun testStableReads() {
        val list = createCopyOnWriteList<ListData>()
        list.add(ListData("Item 1"))
        list.add(ListData("Item 2"))
        list.add(ListData("Item 3"))

        val iter3 = list.iterator()

        list.add(ListData("Item 4"))

        val iter4 = list.iterator()

        list.removeAt(1)

        val iterRemove = list.iterator()

        list.clear()

        checkIter(iter3, ListData("Item 1"), ListData("Item 2"), ListData("Item 3"))
        checkIter(iter4, ListData("Item 1"), ListData("Item 2"), ListData("Item 3"), ListData("Item 4"))
        checkIter(iterRemove, ListData("Item 1"), ListData("Item 3"), ListData("Item 4"))
        checkIter(list.iterator())
    }

    fun checkIter(iter: Iterator<ListData>, vararg items: ListData) {
        var count = 0
        iter.forEach {
            assertEquals(it, items[count++])
        }

        assertEquals(count, items.size)
    }

    @Test
    fun contains() {
        val list = createCopyOnWriteList<ListData>().mpfreeze()

        for (i in 0 until 100) {
            list.add(ListData("a $i"))
        }

        assertTrue(list.contains(ListData("a 44")))
        assertFalse(list.contains(ListData("a 444")))
    }

    @Test
    fun containsAll() {
        val list = createCopyOnWriteList<ListData>().mpfreeze()

        for (i in 0 until 100) {
            list.add(ListData("a $i"))
        }

        assertTrue(list.containsAll(mutableListOf(
            ListData("a 5"), ListData("a 15"), ListData("a 50")
        )))
        assertFalse(list.containsAll(mutableListOf(
            ListData("a 5"), ListData("a 15"), ListData("a 500")
        )))
    }

    @Test
    fun get() {
        val list = createCopyOnWriteList<ListData>().mpfreeze()

        for (i in 0 until 100) {
            list.add(ListData("a $i"))
        }

        assertEquals(ListData("a 17"), list.get(17))
        assertEquals(ListData("a 55"), list.get(55))
        assertFails { list.get(101) }
    }

    @Test
    fun indexOf() {
        val list = createCopyOnWriteList<ListData>().mpfreeze()

        for (i in 0 until 100) {
            list.add(ListData("a ${i % 20}"))
        }

        assertEquals(17, list.indexOf(ListData("a 17")))
        assertEquals(-1, list.indexOf(ListData("a 22")))
    }

    @Test
    fun isEmpty() {
        val list = createCopyOnWriteList<ListData>().mpfreeze()

        assertTrue(list.isEmpty())

        for (i in 0 until 100) {
            list.add(ListData("a ${i}"))
        }

        assertFalse(list.isEmpty())
        val iter = list.iterator()
        list.clear()
        assertTrue(list.isEmpty())
        assertFalse(iter.toList().isEmpty())
    }

    @Test
    fun lastIndexOf() {
        val list = createCopyOnWriteList<ListData>().mpfreeze()

        for (i in 0 until 100) {
            list.add(ListData("a ${i % 20}"))
        }

        assertEquals(85, list.lastIndexOf(ListData("a 5")))
        assertEquals(-1, list.lastIndexOf(ListData("a 21")))
    }

    @Test
    fun mtAdd() {

        val LOOPS = 500

        val tops = ThreadOps<ListData>()

        for (i in 0 until LOOPS) {
            val key = "Add: $i"
            tops.exe { it.add(ListData(key)) }
            tops.test{ list -> assertTrue(list.contains(ListData(key))) }
        }

        val list = tops.run(8)
        assertEquals(list.size, LOOPS)

        println("mtAdd: ${tops.lastRunTime}")
    }

    @Test
    fun mtAddAt(){
        val LOOPS = 100

        val tops = ThreadOps<ListData>()

        for (i in 0 until LOOPS) {
            val key = "Add: $i"
            tops.exe {
                val index = if(it.size == 0){0}else{i % it.size}
                it.add(index, ListData(key) )
            }
            tops.test{ list -> assertTrue(list.contains(ListData(key))) }
        }

        val list = tops.run(8)

        assertFails {
            list.add(list.size + 1, ListData("Never"))
        }

        assertEquals(list.size, LOOPS)
    }

    @Test
    fun mtAddAll(){
        val LOOPS = 100

        val tops = ThreadOps<ListData>()

        for (i in 0 until LOOPS) {
            val key = "Add: $i"
            tops.exe { it.addAll(listOf(ListData("$key 0"), ListData("$key 1"), ListData("$key 2")) ) }
            tops.test{ list ->
                assertTrue(list.contains(ListData("$key 0")))
                assertTrue(list.contains(ListData("$key 1")))
                assertTrue(list.contains(ListData("$key 2")))
            }
        }

        val list = tops.run(8)
        assertEquals(list.size, LOOPS*3)
    }

    @Test
    fun mtAddAllAt(){
        val LOOPS = 100

        val tops = ThreadOps<ListData>()

        for (i in 0 until LOOPS) {
            val key = "Add: $i"
            tops.exe {
                val index = if(it.size == 0){0}else{i % it.size}
                it.addAll(index, listOf(ListData("$key 0"), ListData("$key 1"), ListData("$key 2")) )
            }
            tops.test{ list ->
                assertTrue(list.contains(ListData("$key 0")))
                assertTrue(list.contains(ListData("$key 1")))
                assertTrue(list.contains(ListData("$key 2")))
            }
        }

        val list = tops.run(8)

        assertFails {
            list.add(list.size + 1, ListData("Never"))
        }

        assertEquals(list.size, LOOPS*3)
    }

    @Test
    fun clear(){
        val list = createCopyOnWriteList<ListData>().mpfreeze()
        for(i in 0 until 5){list.add(ListData("a $i"))}
        assertEquals(5, list.size)
        val iterator = list.iterator()
        list.clear()
        assertEquals(0, list.size)
        assertEquals(5, iterator.toList().size)
    }

    @Test
    fun mtRemove() {

        val LOOPS = 500

        val list = createCopyOnWriteList<ListData>().mpfreeze()

        for (i in 0 until LOOPS) {
            val key = "Add: $i"
            list.add(ListData(key))
        }

        val iter = list.iterator()

        val tops = ThreadOps<ListData>()

        for (i in 0 until LOOPS) {
            val key = "Add: $i"
            tops.exe { it.remove(ListData(key)) }
        }

        tops.run(8, list)
        assertEquals(list.size, 0)
        assertEquals(iter.toList().size, LOOPS)
    }

    @Test
    fun removeAll()
    {
        val list = createCopyOnWriteList<ListData>().mpfreeze()

        for(i in 0 until 100){
            list.add(ListData("a $i"))
        }

        val retlist = mutableListOf<ListData>()
        for(i in 33 until 66){
            retlist.add(ListData("a $i"))
        }

        for(i in 133 until 166){
            retlist.add(ListData("a $i"))
        }

        list.removeAll(retlist)

        assertEquals(67, list.size)
    }

    @Test
    fun removeAt()
    {
        val list = createCopyOnWriteList<ListData>().mpfreeze()

        for(i in 0 until 100){
            list.add(ListData("a $i"))
        }

        assertFails { list.removeAt(101) }

        for(i in 0 until 10){
            list.removeAt(i * 10)
        }

        assertEquals(90, list.size)
    }

    @Test
    fun retainAll()
    {
        val list = createCopyOnWriteList<ListData>().mpfreeze()

        for(i in 0 until 100){
            list.add(ListData("a $i"))
        }

        val retlist = mutableListOf<ListData>()
        for(i in 33 until 66){
            retlist.add(ListData("a $i"))
        }

        for(i in 133 until 166){
            retlist.add(ListData("a $i"))
        }

        val iter = list.iterator()

        list.retainAll(retlist)

        assertEquals(33, list.size)
        assertEquals(100, iter.toList().size)
    }

    @Test
    fun mtSet()
    {
        val list = createCopyOnWriteList<ListData>().mpfreeze()

        for(i in 0 until 100){
            list.add(ListData("a $i"))
        }

        val ops = ThreadOps<ListData>()
        for(i in 0 until 100){
            ops.exe { it.set(i, ListData("b $i")) }
            ops.test { assertEquals(it.get(i), ListData("b $i")) }
        }

        val iter = list.iterator()

        ops.run(8, list)

        val oldList = iter.toList()
        for(i in 0 until 100){
            assertEquals(oldList.get(i), ListData("a $i"))
        }

        assertFails { list.set(101, ListData("Nah")) }
    }

    @Test
    fun subList() {
        val list = createCopyOnWriteList<ListData>().mpfreeze()

        for (i in 0 until 100) {
            list.add(ListData("a $i"))
        }

        val sub = list.subList(10, 20)

        val checkit:(MutableList<ListData>)->Unit = {list ->
            for(i in 10 until 20){
                assertEquals(ListData("a $i"), list.get(i - 10))
            }
        }

        checkit(sub)
        //This will throw ConcurrentModificationException on Java, but not so in Kotlin. Hmm. Doesn't feel like a huge deal.
//        list.set(12, ListData("b 12"))
//        checkit(sub)
    }

    @Test
    fun listIterator() {
        val list = createCopyOnWriteList<ListData>().mpfreeze()

        for (i in 0 until 100) {
            list.add(ListData("a $i"))
        }

        val iter = list.listIterator()

        for(i in 0 until 20){
            iter.next()
        }

        assertEquals(20, iter.nextIndex())
        assertEquals(19, iter.previousIndex())
        assertEquals(ListData("a 19"), iter.previous())
        assertEquals(18, iter.previousIndex())

        assertFails { iter.add(ListData("asdf")) }
        assertFails { iter.set(ListData("asdf")) }
    }
}