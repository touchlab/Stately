package co.touchlab.stately

import kotlin.native.concurrent.freeze
import kotlin.native.concurrent.isFrozen

actual fun <T> T.freeze(): T = this.freeze()
actual fun <T> T.isFrozen(): Boolean = this.isFrozen
actual fun <T> T.isNativeFrozen(): Boolean = this.isFrozen

actual val isNative: Boolean = true