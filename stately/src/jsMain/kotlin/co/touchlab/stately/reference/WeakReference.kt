package co.touchlab.stately.reference

/**
 * Multiplatform WeakReference implementation
 */
actual class WeakReference<ValueType: Any> actual constructor(referred: ValueType) {

    private var internalVar: ValueType? = referred

    actual fun get() = internalVar

    actual fun clear() {
        internalVar = null
    }
}