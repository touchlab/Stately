package co.touchlab.stately

import platform.Foundation.NSLock
import kotlin.native.concurrent.*

class AtomicGlobalMutableList<E>():GlobalMutableList<E>{

    private val atomList = AtomicReference<MutableList<E>>(ArrayList<E>().freeze())
    override val size: Int
        get() = atomList.value.size

    private inline fun <R> withModification(proc:(MutableList<E>)->R):R{
        val l = ArrayList(atomList.value)
        val result = proc(l)
        atomList.value = l.freeze()
        return result
    }
    override fun add(element: E): Boolean = withModification { it.add(element) }

    override fun add(index: Int, element: E) = withModification { it.add(index, element) }

    override fun addAll(index: Int, elements: Collection<E>): Boolean = withModification { it.addAll(index, elements) }

    override fun addAll(elements: Collection<E>): Boolean = withModification { it.addAll(elements) }

    override fun clear() = withModification { it.clear() }

    override fun contains(element: E): Boolean = atomList.value.contains(element)

    override fun containsAll(elements: Collection<E>): Boolean = atomList.value.containsAll(elements)

    override fun get(index: Int): E = atomList.value.get(index)

    override fun indexOf(element: E): Int = atomList.value.indexOf(element)

    override fun isEmpty(): Boolean = atomList.value.isEmpty()

    override fun lastIndexOf(element: E): Int = atomList.value.lastIndexOf(element)

    override fun remove(element: E): Boolean = withModification { it.remove(element) }

    override fun removeAll(elements: Collection<E>): Boolean = withModification { it.removeAll(elements) }

    override fun removeAt(index: Int): E = withModification { it.removeAt(index) }

    override fun retainAll(elements: Collection<E>): Boolean = withModification { it.retainAll(elements) }

    override fun set(index: Int, element: E): E = withModification { it.set(index, element) }

    override fun safeClose() {
        clear()
    }

    override fun safeCopy(): List<E> = ArrayList(atomList.value).freeze()

    override fun safeSublist(firstIndex: Int, lastIndex: Int): List<E>
            = ArrayList(atomList.value.subList(firstIndex, lastIndex)).freeze()
}

class LockedGlobalMutableList<E>():GlobalMutableList<E>{

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

    fun close(){
        lock.lock()
        try {
            //Once attached, memory should be cleaned up
            stateBox.value.attach()
        } finally {
            lock.unlock()
        }
    }

    override fun safeClose() {
        close()
    }

    override fun safeCopy(): List<E> = this.listExtract { it }

    override fun safeSublist(firstIndex: Int, lastIndex: Int): List<E> = this.listExtract { it.subList(firstIndex, lastIndex) }

    private fun listExtract(proc:(MutableList<E>)->List<E>):List<E>{
        var result:MutableList<E>? = null
        withLockDetached {
            val extracted = proc(it)
            val r = ArrayList<E>(extracted.size)
            r.addAll(extracted)
            result = r

            return@withLockDetached it
        }

        return result!!.freeze()
    }
}

actual fun <E> sharedList(locked:Boolean): GlobalMutableList<E> = if(locked){LockedGlobalMutableList()}else{AtomicGlobalMutableList()}