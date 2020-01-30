package co.touchlab.stately.isolate

import co.touchlab.kapture.FreezingBlockCall

expect class StateHolder<out T : Any>(t: T) {
    val myThread: Boolean
    val myState: T
    fun remove()
}

open class IsolateState<T : Any> constructor(private val stateHolder: StateHolder<T>) {
    constructor(producer: () -> T) : this(createState(producer))

    @FreezingBlockCall
    fun <R> access(block: (T) -> R): R {

        return if (stateHolder.myThread) {
            block(stateHolder.myState)
        } else {
            stateRun {
                block(stateHolder.myState)
            }
        }
    }

    fun remove() = if (stateHolder.myThread) {
        stateHolder.remove()
    }else {
        stateRun {
            stateHolder.remove()
        }
    }
}

internal expect fun <R> stateRun(block: () -> R):R

fun <T : Any> createState(producer: () -> T): StateHolder<T> = stateRun { StateHolder(producer()) }

internal sealed class RunResult
internal data class Ok<T>(val result:T):RunResult()
internal data class Thrown(val throwable: Throwable):RunResult()