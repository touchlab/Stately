package co.touchlab.stately.collections

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IsoMutableListTest : IsoMutableCollectionTest() {
    override fun defaultCollection(): IsoMutableCollection<SomeData> = IsoMutableList()

    @Test
    fun get() {
        val list = addABunch(threads = 1) as IsoMutableList<SomeData>
        assertEquals(list.get(234), SomeData("key 234"))
        assertEquals(list.get(434), SomeData("key 434"))
    }

    @Test
    fun indexOf() {
        val list = addABunch(threads = 1) as IsoMutableList<SomeData>
        assertEquals(list.indexOf(SomeData("key 234")), 234)
    }

    @Test
    fun lastIndexOf() {
        val list = addABunch(threads = 1) as IsoMutableList<SomeData>
        addABunch(list, threads = 1)
        assertEquals(list.lastIndexOf(SomeData("key 234")), 734)
    }

    @Test
    fun removeAt() {
        val list = addABunch(threads = 1) as IsoMutableList<SomeData>
        assertEquals(list.removeAt(234), SomeData("key 234"))
        assertEquals(list.size, 499)
    }

    @Test
    fun set() {
        val list = addABunch(threads = 1) as IsoMutableList<SomeData>
        assertEquals(list.set(234, SomeData("key 2340")), SomeData("key 234"))
        assertEquals(list.size, 500)
    }

    @Test
    fun subList() {
        val list = addABunch(threads = 1) as IsoMutableList<SomeData>
        assertEquals(
            list.subList(234, 237), listOf(
                SomeData("key 234"),
                SomeData("key 235"),
                SomeData("key 236")
            )
        )
    }

    @Test
    fun listIterator() {
        val list = addABunch() as IsoMutableList<SomeData>
        val reconstruct = mutableListOf<SomeData>()
        list.listIterator().forEach { reconstruct.add(it) }
        assertEquals(reconstruct, list)
    }

    @Test
    fun listIteratorIndex() {
        val list = addABunch() as IsoMutableList<SomeData>
        val reconstruct = mutableListOf<SomeData>()
        list.listIterator(122).forEach { reconstruct.add(it) }
        assertEquals(reconstruct, list.subList(122, list.size))
    }

    @Test
    fun addAll() {
        val set = addABunch()
        assertEquals(set.size, 500)
        set.addAll(listOf(SomeData("a"), SomeData("b"), SomeData("a")))
        assertEquals(set.size, 503)
    }

    @Test
    fun equals() {
        fun makeSome():IsoMutableList<SomeData>{
            val l = IsoMutableList<SomeData>()
            repeat(20){
                l.add(SomeData("key $it"))
            }
            return l
        }
        val l1 = makeSome()
        val l2 = makeSome()
        assertTrue(l1.equals(l2))
    }
}