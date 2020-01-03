package co.touchlab.stately.isolate

import kotlinx.cinterop.StableRef
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.newSingleThreadContext
import kotlin.coroutines.CoroutineContext
import kotlin.native.concurrent.SharedImmutable

@SharedImmutable
internal actual val stateDispatcher: CoroutineDispatcher = newSingleThreadContext("state")

internal actual fun <T> runBlocking(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> T
): T = kotlinx.coroutines.runBlocking(context, block)

/**
 * Do not directly use this. You will have state issues. You can only interact with this class
 * from the state thread.
 */
internal actual class StateHolder<T:Any> actual constructor(t: T) {
    private val stableRef = StableRef.create(t)
    actual val myState: T
        get() = stableRef.get()

    actual fun remove() {
        stableRef.dispose()
    }
}