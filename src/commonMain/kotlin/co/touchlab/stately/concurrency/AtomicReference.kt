package co.touchlab.stately.concurrency

/**
 * Multiplatform AtomicReference implementation
 */
expect class AtomicReference<T>(value_: T) {
    var value:T

    /**
     * Compare current value with expected and set to new if they're the same. Note, 'compare' is checking
     * the actual object id, not 'equals'.
     */
    fun compareAndSet(expected: T, new: T): Boolean
}
