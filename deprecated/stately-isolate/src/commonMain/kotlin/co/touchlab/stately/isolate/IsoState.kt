package co.touchlab.stately.isolate

import kotlin.native.concurrent.SharedImmutable

expect class StateHolder<out T : Any> internal constructor(t: T, stateRunner: StateRunner) {
    val isDisposed: Boolean
    val myThread: Boolean
    val stateRunner: StateRunner
    val myState: T
    fun dispose()
}

open class IsolateState<T : Any> constructor(private val stateHolder: StateHolder<T>) {
    constructor(stateRunner: StateRunner? = null, producer: () -> T) : this(createState(stateRunner, producer))

    val isDisposed: Boolean
        get() = stateHolder.isDisposed

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

@SharedImmutable
internal val defaultStateRunner: BackgroundStateRunner = BackgroundStateRunner()

fun <T : Any> createState(stateRunner: StateRunner?, producer: () -> T): StateHolder<T> {
    val runner = stateRunner ?: defaultStateRunner
    return runner.stateRun { StateHolder(producer(), runner) }
}

/**
 * Hook to shutdown iso-state default runtime
 */
fun shutdownIsoRunner() {
    defaultStateRunner.stop()
}

internal sealed class RunResult
internal data class Ok<T>(val result: T) : RunResult()
internal data class Thrown(val throwable: Throwable) : RunResult()
