package co.touchlab.stately.collections

import kotlin.native.concurrent.freeze

actual fun <T> createCopyOnWriteList(collection: Collection<T>?): MutableList<T> {
    return if(collection == null)
        CopyOnWriteList()
    else
        CopyOnWriteList(collection)
}

actual fun <T> T.mpfreeze(): T = this.freeze()