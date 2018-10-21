package co.touchlab.stately

expect fun <T> T.freeze(): T

expect fun <T> T.isFrozen(): Boolean

/**
 * Will return true on JVM. Strictly speaking, returning true for 'isNative' on JVM would be false,
 * but generally you're checking to ensure threading will be OK.
 */
expect fun <T> T.isNativeFrozen(): Boolean