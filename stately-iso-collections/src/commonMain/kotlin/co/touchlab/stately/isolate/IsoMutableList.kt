package co.touchlab.stately.isolate

class IsoMutableList<T> internal constructor(stateHolder: StateHolder<MutableList<T>>) :
    IsoMutableCollection<T>(stateHolder), MutableList<T> {
    constructor(producer: () -> MutableList<T> = { mutableListOf() }) : this(createState(producer))

    override fun get(index: Int): T = access { asMutableList(it).get(index) }

    private fun asMutableList(it: MutableCollection<T>) = (it as MutableList<T>)

    override fun indexOf(element: T): Int = access { asMutableList(it).indexOf(element) }

    override fun lastIndexOf(element: T): Int = access { asMutableList(it).lastIndexOf(element) }

    override fun add(index: Int, element: T) = access { asMutableList(it).add(index, element) }

    override fun addAll(index: Int, elements: Collection<T>): Boolean = access { asMutableList(it).addAll(index, elements) }

    override fun listIterator(): MutableListIterator<T> =
        access { IsoMutableListIterator(StateHolder(asMutableList(it).listIterator())) }

    override fun listIterator(index: Int): MutableListIterator<T> =
        access { IsoMutableListIterator(StateHolder(asMutableList(it).listIterator(index))) }

    override fun removeAt(index: Int): T = access { asMutableList(it).removeAt(index) }

    override fun set(index: Int, element: T): T = access { asMutableList(it).set(index, element) }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> =
        access { asMutableList(it).subList(fromIndex, toIndex) }
}

class IsoMutableListIterator<T> internal constructor(stateHolder: StateHolder<MutableListIterator<T>>) :
    IsolateState<MutableListIterator<T>>(stateHolder), MutableListIterator<T> {
    override fun hasPrevious(): Boolean = access { it.hasPrevious() }

    override fun nextIndex(): Int = access { it.nextIndex() }

    override fun previous(): T = access { it.previous() }

    override fun previousIndex(): Int = access { it.previousIndex() }

    override fun add(element: T) = access { it.add(element) }

    override fun hasNext(): Boolean = access { it.hasNext() }

    override fun next(): T = access { it.next() }

    override fun set(element: T) = access { it.set(element) }
}