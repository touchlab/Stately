package co.touchlab.stately.native

import kotlin.native.concurrent.DetachedObjectGraph
import kotlin.test.Test
import kotlin.test.assertFails

class TransferExamples {
    @Test
    fun failLocal(){
//        val d = Dat("Hello")
        DetachedObjectGraph {Dat("Hello")}
        /*assertFails {
            DetachedObjectGraph {d}
        }*/
    }

    data class Dat(val s:String)
}