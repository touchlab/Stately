package co.touchlab.stately.isolate

import co.touchlab.stately.ensureNeverFrozen
import co.touchlab.stately.isFrozen
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.native.concurrent.SharedImmutable

@SharedImmutable
internal expect val stateDispatcher: CoroutineDispatcher

internal expect fun <T> runBlocking(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> T
): T

internal expect class StateHolder<T : Any>(t: T) {
    val myState: T
    fun remove()
}

open class IsolateState<T : Any>(producer: () -> T) {
    private val stateHolder: StateHolder<T> = createStateBlocking(producer)

    internal suspend fun <R> access(block: (T) -> R): R = withContext(stateDispatcher) {
        block(stateHolder.myState)
    }

    suspend fun remove() {
        stateHolder.remove()
    }
}

internal fun <T : Any> createStateBlocking(producer: () -> T): StateHolder<T> = runBlocking { createState(producer) }

internal suspend fun <T : Any> createState(producer: () -> T): StateHolder<T> = withContext(stateDispatcher) {
    val t = producer()
    if (t.isFrozen())
        throw IllegalStateException("Mutable state shouldn't be frozen")
    t.ensureNeverFrozen()
    StateHolder(t)
}