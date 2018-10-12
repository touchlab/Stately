package co.touchlab.stately

import java.util.*
import kotlin.collections.ArrayList

open class SharedMutableList<E>():GlobalMutableList<E>{


    override val size: Int
        get() = delegate.size

    override fun add(element: E): Boolean = delegate.add(element)

    override fun add(index: Int, element: E) = delegate.add(index, element)

    override fun addAll(index: Int, elements: Collection<E>): Boolean = delegate.addAll(index, elements)

    override fun addAll(elements: Collection<E>): Boolean = delegate.addAll(elements)

    override fun clear() {
        delegate.clear()
    }

    override fun contains(element: E): Boolean = delegate.contains(element)

    override fun containsAll(elements: Collection<E>): Boolean = delegate.containsAll(elements)

    override fun get(index: Int): E = delegate.get(index)

    override fun indexOf(element: E): Int = delegate.indexOf(element)

    override fun isEmpty(): Boolean = delegate.isEmpty()

    override fun lastIndexOf(element: E): Int = delegate.lastIndexOf(element)

    override fun remove(element: E): Boolean = delegate.remove(element)

    override fun removeAll(elements: Collection<E>): Boolean = delegate.removeAll(elements)

    override fun removeAt(index: Int): E = delegate.removeAt(index)

    override fun retainAll(elements: Collection<E>): Boolean = delegate.retainAll(elements)

    override fun set(index: Int, element: E): E = delegate.set(index, element)

    override fun safeClose() {}

    override fun safeCopy(): List<E> = ArrayList(delegate)

    override fun safeSublist(firstIndex: Int, lastIndex: Int): List<E> = ArrayList(delegate.subList(firstIndex, lastIndex))

    internal val delegate = Collections.synchronizedList(ArrayList<E>())

}

actual fun <E> sharedList(locked:Boolean): GlobalMutableList<E> = SharedMutableList()
