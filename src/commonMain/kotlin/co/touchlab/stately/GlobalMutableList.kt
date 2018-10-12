package co.touchlab.stately

interface GlobalMutableList<E>{
    val size:Int
    fun add(element: E): Boolean
    fun add(index: Int, element: E)
    fun addAll(index: Int, elements: Collection<E>): Boolean
    fun addAll(elements: Collection<E>): Boolean
    fun clear()
    fun contains(element: E): Boolean
    fun containsAll(elements: Collection<E>): Boolean
    fun get(index: Int): E
    fun indexOf(element: E): Int
    fun isEmpty(): Boolean
    fun lastIndexOf(element: E): Int
    fun remove(element: E): Boolean
    fun removeAll(elements: Collection<E>): Boolean
    fun removeAt(index: Int): E
    fun retainAll(elements: Collection<E>): Boolean
    fun set(index: Int, element: E): E
    fun safeClose()
    fun safeCopy():List<E>
    fun safeSublist(firstIndex:Int, lastIndex:Int):List<E>
}

expect fun <E> sharedList(locked:Boolean = false):GlobalMutableList<E>