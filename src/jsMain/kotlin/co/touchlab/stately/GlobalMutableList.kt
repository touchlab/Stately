package co.touchlab.stately

actual fun <E> sharedList(): GlobalMutableList<E> = mutableListOf()
actual fun <E> MutableList<E>.close() {}
actual fun <E> MutableList<E>.iterator(proc: (MutableIterator<E>) -> Unit) {
    proc(this.iterator())
}
actual fun <E> MutableList<E>.listIterator(proc: (MutableListIterator<E>) -> Unit) {
    proc(this.listIterator())
}
actual fun <E> MutableList<E>.listIterator(index: Int, proc: (MutableListIterator<E>) -> Unit) {
    proc(this.listIterator(index))
}
actual fun <E> MutableList<E>.subList(fromIndex: Int, toIndex: Int, proc: (MutableList<E>) -> Unit) {
    proc(this.subList(fromIndex, toIndex))
}

actual class Lock actual constructor() {
    actual fun lock() {}
    actual fun unlock() {}
}

/**
 * For pthread_mutex, which we're not using yet, but will be.
 */
actual fun Lock.close() {}