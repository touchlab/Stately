package co.touchlab.stately.collections

import java.util.concurrent.CopyOnWriteArrayList

actual fun <T> createCopyOnWriteList(collection: Collection<T>?): MutableList<T> {
    return if(collection == null)
        CopyOnWriteArrayList<T>()
    else
        CopyOnWriteArrayList<T>(collection)
}

actual fun <T> T.mpfreeze(): T= this
actual val isNative: Boolean = false
actual fun <T> T.isFrozen(): Boolean = false