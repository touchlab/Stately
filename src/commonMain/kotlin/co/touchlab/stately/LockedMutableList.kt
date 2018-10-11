package co.touchlab.stately

class LockedMutableList<E>(private val delegate:MutableList<E>):MutableList<E>{

    private val lock = Lock()

    internal fun <R> withLock(proc:(MutableList<E>) -> R):R {
        lock.lock()
        try {
            return proc(delegate)
        } finally {
            lock.unlock()
        }
    }

    override val size: Int
        get() = withLock { it.size }

    override fun add(element: E): Boolean = withLock { it.add(element) }

    override fun add(index: Int, element: E) {
        withLock { delegate.add(index, element) }
    }

    override fun addAll(index: Int, elements: Collection<E>): Boolean = withLock { it.addAll(index, elements) }

    override fun addAll(elements: Collection<E>): Boolean = withLock { it.addAll(elements) }

    override fun clear() {
        withLock { it.clear() }
    }

    override fun contains(element: E): Boolean = withLock { it.contains(element) }

    override fun containsAll(elements: Collection<E>): Boolean = withLock { it.containsAll(elements) }

    override fun get(index: Int): E = withLock { it.get(index) }

    override fun indexOf(element: E): Int = withLock { it.indexOf(element) }

    override fun isEmpty(): Boolean = withLock { it.isEmpty() }

    override fun iterator(): MutableIterator<E> {
        throw UnsupportedOperationException()
    }

    override fun lastIndexOf(element: E): Int = withLock { it.lastIndexOf(element) }

    override fun listIterator(): MutableListIterator<E> {
        throw UnsupportedOperationException()
    }

    override fun listIterator(index: Int): MutableListIterator<E> {
        throw UnsupportedOperationException()
    }

    override fun remove(element: E): Boolean = withLock { it.remove(element) }

    override fun removeAll(elements: Collection<E>): Boolean = withLock { it.removeAll(elements) }

    override fun removeAt(index: Int): E = withLock { it.removeAt(index) }

    override fun retainAll(elements: Collection<E>): Boolean = withLock { it.retainAll(elements) }

    override fun set(index: Int, element: E): E = withLock { it.set(index, element) }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> {
        throw UnsupportedOperationException()
    }
}