package co.touchlab.stately.collections.internal

import kotlin.native.internal.GC

internal actual fun gc() {
    GC.collect()
}