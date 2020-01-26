package co.touchlab.stately.isolate

import kotlinx.cinterop.StableRef
import kotlin.native.concurrent.SharedImmutable
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker
import kotlin.native.concurrent.ensureNeverFrozen
import kotlin.native.concurrent.freeze
import kotlin.native.concurrent.isFrozen

/**
 * Do not directly use this. You will have state issues. You can only interact with this class
 * from the state thread.
 */
actual class StateHolder<out T:Any> actual constructor(t: T) {
    private val stableRef :StableRef<T>

    init {
        if (t.isFrozen)
            throw IllegalStateException("Mutable state shouldn't be frozen")
        t.ensureNeverFrozen()
        stableRef = StableRef.create(t)
    }

    actual val myState: T
        get() = stableRef.get()

    actual fun remove() {
        stableRef.dispose()
    }
}

@SharedImmutable
internal val stateWorker = Worker.start(errorReporting = false)

internal actual fun <R> stateRun(block: () -> R): R {
    val result = stateWorker.execute(TransferMode.SAFE, { block.freeze() }, {
        try {
            Ok(it())
        } catch (e: Throwable) {
            Thrown(e)
        }
    }).result
    return when(result){
        is Ok<*> -> result.result as R
        is Thrown -> throw result.throwable
    }
}

