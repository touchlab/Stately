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
/**
 * Will return true on JVM. Strictly speaking, returning true for 'isNative' on JVM would be false,
 * but generally you're checking to ensure threading will be OK.
 */
actual fun <T> T.isNativeFrozen(): Boolean = this.isFrozen