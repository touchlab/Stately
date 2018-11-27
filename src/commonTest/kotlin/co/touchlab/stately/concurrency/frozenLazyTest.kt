package co.touchlab.stately.concurrency

import co.touchlab.stately.freeze
import co.touchlab.stately.isNativeFrozen
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class frozenLazyTest{
    @Test
    fun frozenLazyRead(){
        val testClass = TestClass().freeze()
        assertEquals(testClass.myLazy, TestData("Hello"))
        assertTrue(testClass.myLazy.isNativeFrozen())
    }

    @Test
    fun frozenLazyWrite(){
        val testClass = TestClassVar().freeze()
        assertEquals(testClass.myLazy, TestData("Hello"))
        assertTrue(testClass.myLazy.isNativeFrozen())
        testClass.myLazy = TestData("Goodbye")
        assertEquals(testClass.myLazy, TestData("Goodbye"))
        assertTrue(testClass.myLazy.isNativeFrozen())
    }

    class TestClass{
        val myLazy:TestData by frozenLazy {TestData("Hello")}
    }

    class TestClassVar{
        var myLazy:TestData by frozenLazy {TestData("Hello")}
    }

    data class TestData(val s:String)
}