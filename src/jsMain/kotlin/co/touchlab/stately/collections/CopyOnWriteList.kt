package co.touchlab.stately.collections

class CopyOnWriteList<T>(elements:Collection<T>):ArrayList<T>(elements), MutableList<T>{
    constructor():this(ArrayList<T>(0))
    constructor(initialCapacity: Int = 0):this(ArrayList<T>(initialCapacity))

    override fun iterator(): MutableIterator<T> {
        return LocalIterator(ArrayList(this))
    }

    override fun listIterator(): MutableListIterator<T> {
        return LocalListIterator(ArrayList(this))
    }

    override fun listIterator(index: Int): MutableListIterator<T> {
        return LocalListIterator(ArrayList(this), index)
    }

    private open class LocalIterator<T>(private val list:List<T>, startIndex:Int = 0):MutableIterator<T>{
        var index = startIndex
        override fun hasNext(): Boolean = list.size > index

        override fun next(): T = list.get(index++)

        override fun remove() {
            throw UnsupportedOperationException("Can't mutate list from iterator")
        }
    }

    private class LocalListIterator<T>(private val list:List<T>, startIndex:Int = 0):
        LocalIterator<T>(list, startIndex), MutableListIterator<T>{
        override fun hasPrevious(): Boolean = index > 0

        override fun nextIndex(): Int = index

        override fun previous(): T = list.get(--index)

        override fun previousIndex(): Int = index - 1

        override fun add(element: T) {
            throw UnsupportedOperationException()
        }

        override fun set(element: T) {
            throw UnsupportedOperationException()
        }
    }
}