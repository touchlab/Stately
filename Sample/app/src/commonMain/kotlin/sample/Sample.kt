package sample

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun main() {

}

internal expect fun <T> runBlocking(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> T): T

internal expect fun measureTimeMillis(block: () -> Unit) : Long

data class SomeData(val s:String)
fun makeMyMap():MutableMap<String, SomeData> = mutableMapOf()