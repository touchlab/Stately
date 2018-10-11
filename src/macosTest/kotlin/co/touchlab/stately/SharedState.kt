package co.touchlab.stately

import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker
import kotlin.native.concurrent.freeze
import kotlin.test.Test
import kotlin.test.assertEquals

class SharedStateTest {
    val worker = Worker.start()

    /*@Test
    fun hilo(){
        val ss = SharedState {
            val hm = HashMap<String, String>()
            hm.put("asdf", "qwert")

            hm
        }.freeze()

        worker.execute(TransferMode.SAFE, {
            ss
        }){
            var mapVal:String? = null
            it.access2 {
                mapVal = it.get("asdf")
                return@access2 it
            }

            println("Mapval: $mapVal")
        }.consume {  }
        worker.requestTermination().consume {  }

        assertEquals("qwert", ss.get().get("asdf"))
//        assertEquals("jjjjj", ss.get().asdf)
    } */

    @Test
    fun basicMap(){

        val map = SharedMap<String, String>().freeze()
        map.put("asdf", "qwert")

        worker.execute(TransferMode.SAFE, {
            map
        }){
            println("Mapval: ${it.get("asdf")}")
        }.consume {  }
        worker.requestTermination().consume {  }

        assertEquals("qwert", map.get("asdf"))
//        assertEquals("jjjjj", ss.get().asdf)
    }
}

data class HiLo(val asdf:String)