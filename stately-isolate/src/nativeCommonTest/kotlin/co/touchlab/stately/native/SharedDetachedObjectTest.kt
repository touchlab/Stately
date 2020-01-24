package co.touchlab.stately.native

import co.touchlab.stately.concurrency.Lock
import co.touchlab.stately.concurrency.withLock
import co.touchlab.stately.isolate.IsolateState
import co.touchlab.stately.isolate.runBlocking
import co.touchlab.testhelp.concurrency.ThreadOperations
import kotlin.native.concurrent.AtomicReference
import kotlin.native.concurrent.DetachedObjectGraph
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.attach
import kotlin.native.concurrent.freeze
import kotlin.system.measureTimeMillis
import kotlin.test.Test

class SharedDetachedObjectTest {
//    @Test
    fun basicTest(){
        val detachedObject = SharedDetachedObject { mutableListOf("a", "b")}

        val totalTime = measureTimeMillis {
            val ops = ThreadOperations {}
            repeat(50_000) { rcount ->
                ops.exe {
                    if (rcount % 1000 == 0)
                        println("prepping $rcount")

                    detachedObject.access {
                        val element = "row $rcount"
                        it.add(element)
                        element
                    }
                }
            }


            ops.run(4)
        }

        println("totalTime dag: $totalTime")
        detachedObject.access {
            println(it.size)
            it.size
        }

        detachedObject.clear()
    }

    @Test
    fun isoTest(){
        val iso = IsolateState<MutableList<String>> { mutableListOf("a", "b") }

        val totalTime = measureTimeMillis {
            val ops = ThreadOperations {}
            repeat(50_000) { rcount ->
                ops.exe {
                    if (rcount % 1000 == 0)
                        println("prepping $rcount")

                    runBlocking {
                        println("e 1")

                        iso.access {
                            val element = "row $rcount"
                            println("adding $element")
                            it.add(element)
                            element
                        }

                        println("e 2")
                    }
                }
            }


            ops.run(4)
        }

        println("totalTime iso: $totalTime")
        runBlocking {
            iso.access {
                println(it.size)
                it.size
            }
            iso.remove()
        }
    }

    /*@Test
    fun detach(){
        val value_: DetachedObjectGraph<Any> = DetachedObjectGraph(TransferMode.SAFE) { SomeData("arst") }
        val dag  = AtomicReference<DetachedObjectGraph<Any>?>(value_.freeze())
        val lock = Lock()

        val ops = ThreadOperations {}
        repeat(50_00) { rcount ->
            ops.exe {
                lock.withLock {
                    println("going $rcount")
                    val someData = dag.value!!.attach() as SomeData
                    println("SD $someData")
                    val detachedObjectGraph: DetachedObjectGraph<Any> = DetachedObjectGraph { SomeData("go $rcount") }
                    detachedObjectGraph.freeze()
                    dag.value = detachedObjectGraph
                }
            }
        }

        ops.run(4)

        println("Done ${dag.value!!.attach()}")
        dag.value = null

        println("Done")
    }*/

    data class SomeData(val s:String)
}