package sample

import co.touchlab.stately.freeze
import kotlin.native.concurrent.AtomicInt
import kotlin.native.concurrent.AtomicReference
import kotlin.native.concurrent.DetachedObjectGraph
import kotlin.native.concurrent.TransferMode

class TalkExamplesNative{
    var justCountingStuff:Int = 0

    init {
        backgroundCall {
            //do something
            justCountingStuff++
        }.freeze()
    }



    fun detachState(a:SomeState):DetachedObjectGraph<SomeState>{
        return DetachedObjectGraph(TransferMode.SAFE, {a})
    }

}

val frozenData = FrozenData(
    AtomicInt(1),
    "asdf",
    AtomicReference(
        OtherState(
            1,
            "qwert")
    )
).freeze()

fun update(frozenData: FrozenData){
    frozenData.someCount.increment()

    val otherState = frozenData.someOtherState.value
    val updated = otherState.copy(otherCount = otherState.otherCount+1)
    frozenData.someOtherState.value = updated.freeze()
}

data class FrozenData(
    val someCount:AtomicInt,
    val someString:String,
    val someOtherState:AtomicReference<OtherState>
)

data class OtherState(
    val otherCount:Int,
    val otherString:String
)


