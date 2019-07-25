package co.touchlab.stately.reference

/**
 * Multiplatform WeakReference implementation
 */
expect class WeakReference<ValueType: Any>(referred: ValueType) {

    fun get(): ValueType?

    fun clear()
}