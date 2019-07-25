package co.touchlab.stately.reference

/**
 * Multiplatform WeakReference implementation
 */
actual class WeakReference<ValueType: Any> actual constructor(referred: ValueType) {

    private val internalVal: kotlin.native.ref.WeakReference<ValueType>? = kotlin.native.ref.WeakReference(referred)

    actual fun get(): ValueType? = internalVal?.get()

    actual fun clear() {
        internalVal?.clear()
    }
}