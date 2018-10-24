package sample

import co.touchlab.stately.freeze

class TalkExamples{
    var justCountingStuff:Int = 0

    init {
        backgroundCall {
            //do something
            justCountingStuff++
        }.freeze()
    }




}


data class SomeState(val s:String)


fun backgroundCall(proc:()->Unit){}