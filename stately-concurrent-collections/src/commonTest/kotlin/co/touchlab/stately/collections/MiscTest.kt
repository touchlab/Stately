package co.touchlab.stately.collections

import kotlinx.collections.immutable.toImmutableList
import kotlin.test.Test

class MiscTest {
    @Test
    fun testImmutableConversion(){
        val l = ConcurrentMutableList<SomeData>()
        repeat(20){l.add(SomeData("arst $it"))}
        val iter = l.iterator()
        iter.next()
        l.add(SomeData("Hello"))
        iter.next()
        val il = l.toImmutableList()
        println(il)
    }
}
