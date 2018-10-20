package co.touchlab.stately.collections

expect fun <T> createCopyOnWriteList(collection:Collection<T>? = null):MutableList<T>

fun <T> Iterator<T>.toList():List<T>{
    val result = mutableListOf<T>()
    while (hasNext()) {result.add(next())}
    return result
}

expect fun <T> T.mpfreeze(): T

expect val isNative:Boolean

expect fun <T> T.isFrozen(): Boolean

/**
 * Will return true on JVM. Strictly speaking, returning true for 'isNative' on JVM would be false,
 * but generally you're checking to ensure threading will be OK.
 */
expect fun <T> T.isNativeFrozen(): Boolean