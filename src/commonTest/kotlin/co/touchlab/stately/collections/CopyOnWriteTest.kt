package co.touchlab.stately.collections

import kotlin.test.Test
import kotlin.test.assertEquals

class CopyOnWriteTest{
    @Test
    fun testStableReads(){
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

    fun checkIter(iter:Iterator<ListData>, vararg items:ListData){
        var count = 0
        iter.forEach {
            assertEquals(it, items[count++])
        }

        assertEquals(count, items.size)
    }

}