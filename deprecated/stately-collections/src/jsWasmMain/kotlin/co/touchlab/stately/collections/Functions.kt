package co.touchlab.stately.collections

/**
 * Creates a copy-on-write list. On the JVM, will return
 * [https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/CopyOnWriteArrayList.html](CopyOnWriteArrayList).
 * On Native, there is a native implementation which maintains and updates a frozen ArrayList.
 */
actual fun <T> frozenCopyOnWriteList(collection: Collection<T>?): MutableList<T> {
    throw UnsupportedOperationException("Not for JS or Wasm")
}
