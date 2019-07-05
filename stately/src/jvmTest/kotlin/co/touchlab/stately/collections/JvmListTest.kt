package co.touchlab.stately.collections

import org.junit.Test
import java.util.*

class JvmListTest {
  /*  @Test
    fun concurrentListIter(){
        val l = Collections.synchronizedList(LinkedList<ListData>())
        (0 until 10).forEach { l.add(ListData("i $it")) }
        val iter1 = l.listIterator()
        println(iter1.next())
        println(iter1.next())
        println(iter1.next())
        l.removeAt(0)
        println(iter1.next())
    }*/
}

data class ListData(val s: String)