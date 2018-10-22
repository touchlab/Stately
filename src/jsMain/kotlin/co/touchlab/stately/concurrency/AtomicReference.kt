package co.touchlab.stately.concurrency

/**
 * Multiplatform AtomicReference implementation
 */
actual class AtomicReference<T> actual constructor(value_: T) {
    actual var value: T = value_

    /**
     * Compare current value with expected and set to new if they're the same. Note, 'compare' is checking
     * the actual object id, not 'equals'.
     */
    actual fun compareAndSet(expected: T, new: T): Boolean {
        return if(expected === value){
            value = new
            true
        }else{
            false
        }
    }

}