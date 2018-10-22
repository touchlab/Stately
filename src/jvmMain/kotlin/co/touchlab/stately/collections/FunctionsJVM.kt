package co.touchlab.stately.collections

import java.util.concurrent.CopyOnWriteArrayList

actual fun <T> frozenCopyOnWriteList(collection: Collection<T>?): MutableList<T> {
    return if(collection == null)
        CopyOnWriteArrayList<T>()
    else
        CopyOnWriteArrayList<T>(collection)
}
