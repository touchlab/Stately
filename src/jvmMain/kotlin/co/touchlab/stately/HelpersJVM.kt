package co.touchlab.stately

actual fun <T> T.freeze(): T= this
actual fun <T> T.isFrozen(): Boolean = false
/**
 * Will return true on JVM. Strictly speaking, returning true for 'isNative' on JVM would be false,
 * but generally you're checking to ensure threading will be OK.
 */
actual fun <T> T.isNativeFrozen(): Boolean = true