package co.touchlab.stately

actual fun <T> T.freeze(): T= this
actual fun <T> T.isFrozen(): Boolean = false
actual fun <T> T.isNativeFrozen(): Boolean = true

actual val isNative: Boolean = false