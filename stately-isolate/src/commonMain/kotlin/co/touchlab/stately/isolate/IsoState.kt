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

expect class StateHolder<T : Any>(t: T) {
    val myState: T
    fun remove()
}

open class IsolateState<T : Any> constructor(private val stateHolder: StateHolder<T>) {
    constructor(producer: () -> T) : this(createStateBlocking(producer))

    internal suspend fun <R> access(block: (T) -> R): R = withContext(stateDispatcher) {
        block(stateHolder.myState)
    }

    internal fun <R> blockingAccess(block: (T) -> R): R = runBlocking { access(block) }

    suspend fun remove() = withContext(stateDispatcher) {
        stateHolder.remove()
    }
}

internal fun <T : Any> createStateBlocking(producer: () -> T): StateHolder<T> = runBlocking { createState(producer) }

internal suspend fun <T : Any> createState(producer: () -> T): StateHolder<T> = withContext(stateDispatcher) {
    val t = producer()
    createStateDirect(t)
}

fun <T : Any> createStateDirect(t: T): StateHolder<T> {
    if (t.isFrozen())
        throw IllegalStateException("Mutable state shouldn't be frozen")
    t.ensureNeverFrozen()
    return StateHolder(t)
}