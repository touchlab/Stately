package co.touchlab.stately.collections

import co.touchlab.testhelp.concurrency.ThreadOperations
import kotlin.math.max
import kotlin.test.Test

class ConcurrentStressTests {
    @Test
    fun listAddRemove(){
        val ops = ThreadOperations {FastNativeLinkedList<ListVals>()}
        (0 until 50).forEach {
            ops.exe {list ->
                (0 until 100).forEach {
                    list.add(ListVals("item $it"))
                }

                (0 until 10).forEach {
                    list.removeAt(0)
                }

                println("Test val ${list.get(0)} size ${list.size}")
            }
        }

        ops.run(5)
    }



    @Test
    fun mapAddRemove(){
        val ops = ThreadOperations {FastNativeHashMap<String, ListVals>()}
        (0 until 100000).forEach {outerCount ->
            ops.exe {list ->
                val countFloor = outerCount * 30
                (0 until 100).forEach {innerCount ->
                    val myCount = countFloor + innerCount
                    list.put("key $myCount", ListVals("item $myCount"))
                }

                (0 until 10).forEach {
                    list.remove("key ${countFloor + it}")
                }


                /*(0 until 10).forEach {
                    val first = list.keys.first()
                    list.remove(first)
                }*/

                if(outerCount % 1000 == 0) {
                    println("Test map count $outerCount size ${list.size}")
                    list.clear()
                }
            }
        }

        ops.run(5)
    }

}

data class ListVals(val s:String)