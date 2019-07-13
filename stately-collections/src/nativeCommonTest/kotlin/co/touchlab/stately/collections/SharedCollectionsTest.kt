package co.touchlab.stately.collections

import co.touchlab.stately.concurrency.Lock
import co.touchlab.stately.concurrency.withLock
import platform.posix.usleep
import kotlin.native.concurrent.FutureState
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker
import kotlin.native.concurrent.freeze
import kotlin.test.Test
import kotlin.test.assertEquals

class SharedCollectionsTest {


    @Test
    fun arst(){
        val workers = Array(8){Worker.start()}
        val lock = Lock().freeze()
        val list = mutableListOf<Arst>()//frozenLinkedList<Arst>()//sharedList<Arst>()
        val futures = workers.map {
            it.execute(TransferMode.UNSAFE, { Pair(list, lock) }) {pair ->
                for(i in 0 until 1_000_000){

                    if(listSize(pair.first) < 100_000_000) {
                        while (!pair.second.tryLock())
                            listSize(pair.first)

                        try {
                            addVal(pair.first, Arst("i $i").freeze())
                        } finally {
                            pair.second.unlock()
                        }

                    }
                }
            }
        }

        while (!futures.all { it.state == FutureState.COMPUTED }) {
            usleep(100.toUInt() * 1000.toUInt())
        }

        workers.forEach { it.requestTermination() }

        assertEquals(8_000_000, list.size)
    }
}

private fun addVal(list:MutableList<Arst>, arst:Arst){
    list.add(arst)
}

private fun listSize(list:List<Arst>):Int = list.size

data class Arst(val s:String)