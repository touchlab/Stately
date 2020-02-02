package co.touchlab.stately.collections.internal

internal actual fun gc() {
    Runtime.getRuntime().gc()
}