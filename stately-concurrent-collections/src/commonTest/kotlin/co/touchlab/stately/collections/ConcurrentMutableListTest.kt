package co.touchlab.stately.collections

import co.touchlab.stately.concurrency.ThreadRef
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ConcurrentMutableListTest {
    @Test
    @NoJsTest
    fun tryConcurrent() {
        val threadRef = ThreadRef()
        val list = ConcurrentMutableList<SomeData>()

        runAlot {
            list.add(SomeData("arst $it"))
            if (it % (DEFAULT_RUNS / 10) == 0)
                println("count $it thread: ${threadRef.same()}")
        }

        assertEquals(list.size, DEFAULT_RUNS * 2)
    }

    @Test
    @NoJsTest
    fun tryBlock() {
        val list = ConcurrentMutableList<SomeData>()

        runAlot(100) { outerCount ->
            list.block {
                repeat(1000) { innerCount ->
                    list.add(SomeData("arst ${innerCount}"))
                }
            }
        }

        assertEquals(list.size, DEFAULT_RUNS * 2)
    }
}

fun runAlot(runs: Int = DEFAULT_RUNS, block: (Int) -> Unit) = runTest {
    val job = async(backgroundDispatcher) {
        repeat(runs) {
            block(it + runs)
        }
    }

    repeat(runs) {
        block(it)
    }

    job.await()
}

data class SomeData(val s: String)

expect val backgroundDispatcher: CoroutineDispatcher

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
expect annotation class NoJsTest()