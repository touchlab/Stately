package co.touchlab.stately.collections

import kotlin.native.concurrent.freeze
import kotlin.native.concurrent.isFrozen

actual fun <T> createCopyOnWriteList(collection: Collection<T>?): MutableList<T> {
    return if(collection == null)
        CopyOnWriteList()
    else
        CopyOnWriteList(collection)
}

actual fun <T> T.mpfreeze(): T = this.freeze()
actual val isNative: Boolean = true
actual fun <T> T.isFrozen(): Boolean = this.isFrozen