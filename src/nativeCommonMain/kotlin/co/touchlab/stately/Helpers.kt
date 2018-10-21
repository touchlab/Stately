package co.touchlab.stately

import kotlin.native.concurrent.freeze
import kotlin.native.concurrent.isFrozen

actual fun <T> T.freeze(): T = this.freeze()

actual fun <T> T.isFrozen(): Boolean = this.isFrozen
/**
 * Will return true on JVM. Strictly speaking, returning true for 'isNative' on JVM would be false,
 * but generally you're checking to ensure threading will be OK.
 */
actual fun <T> T.isNativeFrozen(): Boolean = this.isFrozen