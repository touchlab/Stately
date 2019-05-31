package co.touchlab.stately.platform

import co.touchlab.stately.collections.FastNativeLinkedList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


class FastListTest {
    @Test
    fun addAtEnd() {
        verifyFunctionality { l ->
            makeList(l).add(10, Dat("arst"))
        }
    }

    @Test
    fun addAtStart() {
        verifyFunctionality {
            makeList(it).add(0, Dat("arst"))
        }

        verifyFunctionality { it.add(0, Dat("arst")) }
    }

    @Test
    fun iterRemove() {
        verifyFunctionality {
            makeList(it)
            val iter = it.listIterator()
            var count = 0
            while (iter.hasNext()) {
                iter.next()
                if (count++ % 3 == 0)
                    iter.remove()
            }
        }
    }


    @Test
    fun getAfterEndOOB() {
        verifyFunctionality {
            makeList(it)
            assertFailsWith(IndexOutOfBoundsException::class) {
                it.get(10)
            }
        }
    }

    @Test
    fun setAtEndOOB() {
        verifyFunctionality {
            makeList(it)
            assertFailsWith(IndexOutOfBoundsException::class) {
                it.set(10, Dat("a"))
            }
        }
    }

    @Test
    fun negativeIndexOOB() {
        verifyFunctionality {
            makeList(it)
            assertFailsWith(IndexOutOfBoundsException::class) {
                it.set(-1, Dat("a"))
            }
        }
    }

    @Test
    fun stableIterator() {
        verifyFunctionality(true) {
            makeList(it)
            val fiveIter = it.listIterator()
            (0 until 5).forEach {
                fiveIter.next()
            }

            assertEquals(fiveIter.next(), Dat("d 5"))
            it.removeAt(0)
            assertEquals(fiveIter.next(), Dat("d 6"))
            checkLast(fiveIter, Dat("d 9"))
        }
    }

    private fun checkLast(iter: ListIterator<Dat>, d: Dat) {
        var last: Dat? = null
        while (iter.hasNext()) {
            last = iter.next()
        }

        assertEquals(last, d)
    }

    fun makeList(l: MutableList<Dat>, count: Int = 10): MutableList<Dat> {
        for (i in 0 until count) {
            l.add(Dat("d $i"))
        }
        return l
    }

    fun verifyFunctionality(onlyLl: Boolean = false, block: (l: MutableList<Dat>) -> Unit) {
        val ll = FastNativeLinkedList<Dat>()
        block(ll)

        if (!onlyLl) {
            val al = ArrayList<Dat>()
            block(al)

            assertEquals(al.size, ll.size, "al: $al, ll: $ll")
            for (i in 0 until al.size) {
                assertEquals(al.get(i), ll.get(i), "Index $i compare failed")
            }
        }
    }
}

//class FastListTest {

/* @Test
 fun checkStandardFunctionality() {
     val l = ArrayList<Dat>()
     for (i in 0 until 10) {
         l.add(Dat("a $i"))
     }

     l.add(10, Dat("arst"))
 }*/

/*@Test
fun basicListOps() {
    val l = FastNativeLinkedList<Dat>()
    for (i in 0 until 1000) {
        l.add(Dat("d $i"))
    }

    for (i in 0 until 20) {
        l.removeAt(i * 20)
    }

    assertEquals(980, l.size)
}

@Test
fun outOfBounds() {
    val testList = testList()
    assertFails {
        testList.get(10)
    }

    assertFails { testList.listIterator().previous() }

    val iter = testList.listIterator()
    iter.forEach { }
    assertFalse(iter.hasNext())
    assertFails { iter.next() }

    assertEquals(10, testList.size)

    println("before add")
    testList.add(10, Dat("d 10"))
    println("after add")
//        assertEquals(11, testList.size)

    assertFails {
        testList.add(12, Dat("d 12"))
    }
}

@Test
fun contains() {
    assertTrue(testList().contains(Dat("d 7")))
}*/

/*private fun testList(): MutableList<Dat> {
    val l = FastNativeLinkedList<Dat>()
    for (i in 0 until 10) {
        l.add(Dat("d $i"))
    }

    return l
}*/
//}

data class Dat(val s: String)