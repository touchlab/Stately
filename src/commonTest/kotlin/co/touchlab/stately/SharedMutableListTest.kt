package co.touchlab.stately

import kotlin.test.Test

class SharedMutableListTest{
    @Test
    fun sharedListTest(){
        val list = sharedList<TestData>()
        list.add(TestData("a"))
        list.add(TestData("b"))
        list.add(TestData("c"))
    }
}

data class TestData(val s:String)