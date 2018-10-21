package co.touchlab.stately.collections

/**
 * Creates a copy-on-write list. On the JVM, will return
 * [https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/CopyOnWriteArrayList.html](CopyOnWriteArrayList).
 * On Native, there is a native implementation which maintains and updates a frozen ArrayList.
 */
expect fun <T> createCopyOnWriteList(collection:Collection<T>? = null):MutableList<T>

/**
 * Creates a list from an Iterator.
 */
fun <T> Iterator<T>.toList():List<T>{
    val result = mutableListOf<T>()
    while (hasNext()) {result.add(next())}
    return result
}