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

    @Test
    fun sharedListCompare(){
        println("Time for locked ${runShared(sharedList(true))}")
        println("Time for atomic ${runShared(sharedList(false))}")
    }

    fun runShared(l:GlobalMutableList<TestData>):Results{

        var start = currentTimeMillis()

        val runs = 2000
        val maxSize = 40
        val minSize = 20
        for(i in 0 until runs){
            l.add(TestData("My run is $i"))
            if(l.size > 40){
                l.removeAll(l.safeSublist(0, minSize))
            }
        }

        val create = currentTimeMillis() - start

        start = currentTimeMillis()

        for(i in 0 until l.size){
            if(l.get(i).s.length == 0)
                throw IllegalStateException()
        }

        val loop = currentTimeMillis() - start
        start = currentTimeMillis()

        for(i in 1..100) {
            for (i in 0 until l.size) {
                if (l.get(i).s.length == 0)
                    throw IllegalStateException()
            }
        }

        val loopLots = currentTimeMillis() - start

        /*
        while(l.size > 0){
            l.removeAt(0)
        }*/

        l.safeClose()

        return Results(create, loop, loopLots)
    }

    data class Results(val create:Long, val loop:Long, val loopLots:Long)
}

data class TestData(val s:String)

expect fun currentTimeMillis():Long