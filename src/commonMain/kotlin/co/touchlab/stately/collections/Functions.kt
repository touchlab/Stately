package co.touchlab.stately.collections

/**
 * Creates a copy-on-write list. On the JVM, will return
 * [https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/CopyOnWriteArrayList.html](CopyOnWriteArrayList).
 * On Native, there is a native implementation which maintains and updates a frozen ArrayList.
 */
expect fun <T> frozenCopyOnWriteList(collection:Collection<T>? = null):MutableList<T>

fun <T> frozenLinkedList(stableIterator:Boolean = false):MutableList<T> = if(stableIterator){
    CopyOnIterateLinkedList()
}else{
    SharedLinkedList()
}

fun <K, V> frozenHashMap(initialCapacity:Int = 16, loadFactor:Float = 0.75.toFloat()):MutableMap<K, V> =
        SharedHashMap(initialCapacity, loadFactor)

fun <K, V> frozenLruCache(maxCacheSize:Int, onRemove:(MutableMap.MutableEntry<K, V>) -> Unit = {}):LruCache<K, V> =
        SharedLruCache(maxCacheSize, onRemove)

/**
 * Creates a list from an Iterator.
 */
fun <T> Iterator<T>.toList():List<T>{
    val result = mutableListOf<T>()
    while (hasNext()) {result.add(next())}
    return result
}