package co.touchlab.stately.platform

import kotlin.test.fail

// just a static type check
fun <T> assertStaticTypeIs(@Suppress("UNUSED_PARAMETER") value:  T) {}

inline fun <reified T> assertStaticAndRuntimeTypeIs(value:  T) {
    @Suppress("USELESS_CAST")
    if ((value as Any?) !is T) {
        fail("Expected value $value to have ${T::class} type")
    }
}