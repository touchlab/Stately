package co.touchlab.stately

import java.lang.UnsupportedOperationException
import java.util.*
import kotlin.collections.ArrayList

open class SharedMutableList<E>():ArrayList<E>(){
    override fun iterator(): MutableIterator<E> {
        throw UnsupportedOperationException()
    }

    override fun listIterator(): MutableListIterator<E> {
        throw UnsupportedOperationException()
    }

    override fun listIterator(index: Int): MutableListIterator<E> {
        throw UnsupportedOperationException()
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> {
        throw UnsupportedOperationException()
    }
}

actual fun <E> MutableList<E>.close() {}
actual fun <E> sharedList(): MutableList<E> = Collections.synchronizedList(SharedMutableList())
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