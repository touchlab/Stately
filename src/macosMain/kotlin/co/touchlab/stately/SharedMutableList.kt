package co.touchlab.stately

import platform.Foundation.NSLock
import kotlin.native.concurrent.*

class SharedMutableList<E>():MutableList<E>{

    private val stateBox: AtomicReference<DetachedObjectGraph<Any>>
            = AtomicReference(DetachedObjectGraph(mode = TransferMode.SAFE, producer = { mutableListOf<E>() as Any}))
    private val lock = NSLock()

    internal fun withLockDetached(proc:(MutableList<E>) -> MutableList<E>) {
        lock.lock()
        try {
            stateBox.value = DetachedObjectGraph(mode = TransferMode.SAFE, producer = {
                val dataList = stateBox.value.attach() as MutableList<E>
                proc(dataList) as Any
            })
        } finally {
            lock.unlock()
        }
    }

    override val size: Int
        get() {
            var sizeVal = 0
            withLockDetached {
                sizeVal = it.size
                return@withLockDetached it
            }

            return sizeVal
        }

    override fun contains(element: E): Boolean {
        var b = false
        withLockDetached {
            b = it.contains(element)
            return@withLockDetached it
        }
        return b
    }

    override fun containsAll(elements: Collection<E>): Boolean {
        var b = false
        withLockDetached {
            b = it.containsAll(elements)
            return@withLockDetached it
        }
        return b
    }

    override fun get(index: Int): E {
        var b : E? = null
        withLockDetached {
            b = it.get(index)
            return@withLockDetached it
        }
        return b!!
    }

    override fun indexOf(element: E): Int {
        var b = 0
        withLockDetached {
            b = it.indexOf(element)
            return@withLockDetached it
        }
        return b
    }

    override fun isEmpty(): Boolean {
        var b = false
        withLockDetached {
            b = it.isEmpty()
            return@withLockDetached it
        }
        return b
    }

    override fun iterator(): MutableIterator<E> {
        throw UnsupportedOperationException()
    }

    override fun lastIndexOf(element: E): Int {
        var b = 0
        withLockDetached {
            b = it.lastIndexOf(element)
            return@withLockDetached it
        }
        return b
    }

    override fun add(element: E): Boolean {
        var b = false
        element.freeze()
        withLockDetached {
            b = it.add(element)
            return@withLockDetached it
        }
        return b
    }

    override fun add(index: Int, element: E) {
        element.freeze()
        withLockDetached {
            it.add(index, element)
            return@withLockDetached it
        }
    }

    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        var b = false
        elements.forEach { it.freeze() }
        withLockDetached {
            b = it.addAll(index, elements)
            return@withLockDetached it
        }
        return b
    }

    override fun addAll(elements: Collection<E>): Boolean {
        var b = false
        elements.forEach { it.freeze() }
        withLockDetached {
            b = it.addAll(elements)
            return@withLockDetached it
        }
        return b
    }

    override fun clear() {
        withLockDetached {
            it.clear()
            return@withLockDetached it
        }
    }

    override fun listIterator(): MutableListIterator<E> {
        throw UnsupportedOperationException()
    }

    override fun listIterator(index: Int): MutableListIterator<E> {
        throw UnsupportedOperationException()
    }

    override fun remove(element: E): Boolean {
        var b = false
        withLockDetached {
            b = it.remove(element)
            return@withLockDetached it
        }
        return b
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        var b = false
        withLockDetached {
            b = it.removeAll(elements)
            return@withLockDetached it
        }
        return b
    }

    override fun removeAt(index: Int): E {
        var b : E? = null
        withLockDetached {
            b = it.removeAt(index)
            return@withLockDetached it
        }
        return b!!
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        var b = false
        withLockDetached {
            b = it.retainAll(elements)
            return@withLockDetached it
        }
        return b
    }

    override fun set(index: Int, element: E): E {
        var b : E? = null
        element.freeze()
        withLockDetached {
            b = it.set(index, element)
            return@withLockDetached it
        }
        return b!!
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> {
        throw UnsupportedOperationException()
    }

    fun close(){
        lock.lock()
        try {
            //Once attached, memory should be cleaned up
            stateBox.value.attach()
        } finally {
            lock.unlock()
        }
    }
}

actual fun <E> MutableList<E>.close() {
    val sml = this as SharedMutableList<E>
    sml.close()
}

actual fun <E> sharedList(): MutableList<E> = SharedMutableList()

actual fun <E> MutableList<E>.iterator(proc:(MutableIterator<E>)->Unit){
    val sml = this as SharedMutableList<E>
    sml.withLockDetached {
        proc(it.iterator())
        return@withLockDetached it
    }
}

actual fun <E> MutableList<E>.listIterator(proc:(MutableListIterator<E>)->Unit){
    val sml = this as SharedMutableList<E>
    sml.withLockDetached {
        proc(WrappedMutableListIterator(it.listIterator()))
        return@withLockDetached it
    }
}

actual fun <E> MutableList<E>.listIterator(index: Int, proc:(MutableListIterator<E>)->Unit){
    val sml = this as SharedMutableList<E>
    sml.withLockDetached {
        proc(WrappedMutableListIterator(it.listIterator(index)))
        return@withLockDetached it
    }
}

actual fun <E> MutableList<E>.subList(fromIndex: Int, toIndex: Int, proc: (MutableList<E>) -> Unit) {
    val sml = this as SharedMutableList<E>
    sml.withLockDetached {
        proc(it.subList(fromIndex, toIndex))
        for (e in it) {
            e.freeze()
        }
        return@withLockDetached it
    }
}

internal class WrappedMutableListIterator<E>(val iter:MutableListIterator<E>):MutableListIterator<E>{
    override fun hasPrevious(): Boolean = iter.hasPrevious()

    override fun nextIndex(): Int = iter.nextIndex()

    override fun previous(): E = iter.previous()

    override fun previousIndex(): Int = iter.previousIndex()

    override fun add(element: E) {
        iter.add(element.freeze())
    }

    override fun hasNext(): Boolean = iter.hasNext()

    override fun next(): E = iter.next()

    override fun remove() {
        iter.remove()
    }

    override fun set(element: E) {
        iter.set(element.freeze())
    }
}