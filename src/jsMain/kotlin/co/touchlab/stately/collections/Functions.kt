package co.touchlab.stately.collections

actual fun <T> frozenCopyOnWriteList(collection: Collection<T>?): MutableList<T> {
    return if(collection == null)
        CopyOnWriteList()
    else
        CopyOnWriteList(collection)
}