package co.touchlab.stately.isolate

expect class StateHolder<out T : Any> internal constructor(t: T, stateRunner: StateRunner) {
    val myThread: Boolean
    val stateRunner: StateRunner
    val myState: T
    fun dispose()
}

open class IsolateState<T : Any> constructor(private val stateHolder: StateHolder<T>) {
    constructor(producer: () -> T) : this(createState(producer))

    fun <R : Any> fork(r: R): StateHolder<R> = if (stateHolder.myThread) {
        StateHolder(r, stateHolder.stateRunner)
    } else {
        throw IllegalStateException("Must fork state from the state thread")
    }

    fun <R> access(block: (T) -> R): R {
        return if (stateHolder.myThread) {
            block(stateHolder.myState)
        } else {
            stateHolder.stateRunner.stateRun {
                block(stateHolder.myState)
            }
        }
    }

    fun dispose() = if (stateHolder.myThread) {
        stateHolder.dispose()
    } else {
        stateHolder.stateRunner.stateRun {
            stateHolder.dispose()
        }
    }
}

internal expect val defaultStateRunner: StateRunner

fun <T : Any> createState(producer: () -> T, stateRunner: StateRunner = defaultStateRunner): StateHolder<T> =
    stateRunner.stateRun { StateHolder(producer(), stateRunner) }

/**
 * Hook to shutdown iso-state default runtime
 */
fun shutdownIsoRunner() {
    defaultStateRunner.stop()
}

internal sealed class RunResult
internal data class Ok<T>(val result: T) : RunResult()
internal data class Thrown(val throwable: Throwable) : RunResult()
