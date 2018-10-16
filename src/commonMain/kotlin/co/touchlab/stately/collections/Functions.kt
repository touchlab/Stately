package co.touchlab.stately.collections

expect fun <T> createCopyOnWriteList(collection:Collection<T>? = null):MutableList<T>

expect fun <T> T.mpfreeze(): T