package co.touchlab.stately.platform

import co.touchlab.stately.collections.NativeMutableList
import co.touchlab.stately.collections.nativeEmptyList
import co.touchlab.stately.collections.nativeListOf
import co.touchlab.stately.collections.toNativeMutableList
import kotlin.random.Random
import kotlin.test.*

class MutableCollectionTest {
    fun <T, C : MutableCollection<T>> testOperation(before: List<T>, after: List<T>, expectedModified: Boolean, toMutableCollection: (List<T>) -> C) =
        fun(operation: (C.() -> Boolean)) {
        val list = toMutableCollection(before)
            assertEquals(expectedModified, list.operation())
            assertEquals(toMutableCollection(after), list)
        }

    fun <T> testOperation(before: List<T>, after: List<T>, expectedModified: Boolean) =
        testOperation(before, after, expectedModified, { it.toNativeMutableList() })


    @Test fun addAll() {
        val data = listOf("foo", "bar")

        testOperation(emptyList(), data, true).let { assertAdd ->
            assertAdd { addAll(data) }
            assertAdd { addAll(data.toTypedArray()) }
            assertAdd { addAll(data.toTypedArray().asIterable()) }
            assertAdd { addAll(data.asSequence()) }
        }

        testOperation(data, data, false, { it.toCollection(LinkedHashSet()) }).let { assertAdd ->
            assertAdd { addAll(data) }
            assertAdd { addAll(data.toTypedArray()) }
            assertAdd { addAll(data.toTypedArray().asIterable()) }
            assertAdd { addAll(data.asSequence()) }
        }
    }

    @Test fun removeAll() {
        val content = nativeListOf("foo", "bar", "bar")
        val data = nativeListOf("bar")
        val expected = nativeListOf("foo")

        testOperation(content, expected, true).let { assertRemove ->
            println("b 1")
            println("checking ${data.size}")
            for(e in data){
                println("checking $e")
            }

            val iter = data.iterator()
            while (iter.hasNext()){
                println("checking iter ${iter.next()}")
            }
            assertRemove { val removeAllResult = removeAll(data)

                println("b $removeAllResult ${ArrayList(data)}")
                removeAllResult
            }
            println("b 2")
            assertRemove { removeAll(data.toTypedArray()) }
            println("b 3")
            assertRemove { removeAll(data.toTypedArray().asIterable()) }
            println("b 4")
            assertRemove { removeAll { it in data } }
            println("b 5")
            assertRemove { (this as MutableIterable<String>).removeAll { it in data } }
            println("b 6")
            val predicate = { cs: CharSequence -> cs.first() == 'b' }
            assertRemove { removeAll(predicate) }
        }


        testOperation(content, content, false).let { assertRemove ->
            assertRemove { removeAll(nativeEmptyList()) }
            assertRemove { removeAll(emptyArray()) }
            assertRemove { removeAll(emptySequence()) }
            assertRemove { removeAll { false } }
            assertRemove { (this as MutableIterable<String>).removeAll { false } }
        }
    }

    @Test fun retainAll() {
        val content = nativeListOf("foo", "bar", "bar")
        val expected = nativeListOf("bar", "bar")

        testOperation(content, expected, true).let { assertRetain ->
            val data = nativeListOf("bar")
            println("a 1")
            assertRetain { retainAll(data) }
            println("a 2")
            assertRetain { retainAll(data.toTypedArray()) }
            println("a 3")
            assertRetain { retainAll(data.toTypedArray().asIterable()) }
            println("a 4")
            assertRetain { retainAll(data.asSequence()) }
            println("a 5")
            assertRetain { retainAll { it in data } }
            println("a 6")
            assertRetain { (this as MutableIterable<String>).retainAll { it in data } }
            println("a 7")

            val predicate = { cs: CharSequence -> cs.first() == 'b' }
            assertRetain { retainAll(predicate) }
        }
        testOperation(content, nativeEmptyList(), true).let { assertRetain ->
            val data = nativeEmptyList<String>()
            assertRetain { retainAll(data) }
            assertRetain { retainAll(data.toTypedArray()) }
            assertRetain { retainAll(data.toTypedArray().asIterable()) }
            assertRetain { retainAll(data.asSequence()) }
            assertRetain { retainAll { it in data } }
            assertRetain { (this as MutableIterable<String>).retainAll { it in data } }
        }
        testOperation(nativeEmptyList<String>(), nativeEmptyList(), false).let { assertRetain ->
            val data = nativeEmptyList<String>()
            assertRetain { retainAll(data) }
            assertRetain { retainAll(data.toTypedArray()) }
            assertRetain { retainAll(data.toTypedArray().asIterable()) }
            assertRetain { retainAll(data.asSequence()) }
            assertRetain { retainAll { it in data } }
            assertRetain { (this as MutableIterable<String>).retainAll { it in data } }
        }
    }

    @Test fun listFill() {
        val list = NativeMutableList(3) { it }
        list.fill(42)
        assertEquals(nativeListOf(42, 42, 42), list)
    }

    @Test fun shuffled() {
        val list = NativeMutableList(100) { it }
        val shuffled = list.shuffled()

        assertNotEquals(list, shuffled)
        assertEquals(list.toSet(), shuffled.toSet())
        assertEquals(list.size, shuffled.distinct().size)
    }

    @Test
    fun shuffledPredictably() {
        val list = List(10) { it }
        val shuffled1 = list.shuffled(Random(1))
        val shuffled11 = list.shuffled(Random(1))

        assertEquals(shuffled1, shuffled11)
        assertEquals("[1, 4, 0, 6, 2, 8, 9, 7, 3, 5]", shuffled1.toString())

        val shuffled2 = list.shuffled(Random(42))
        assertEquals("[5, 0, 4, 9, 2, 8, 1, 7, 6, 3]", shuffled2.toString())
    }

}
