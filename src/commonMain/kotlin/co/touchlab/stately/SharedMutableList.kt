package co.touchlab.stately

expect fun <E> sharedList():MutableList<E>

expect fun <E> MutableList<E>.close()
expect fun <E> MutableList<E>.iterator(proc:(MutableIterator<E>)->Unit)
expect fun <E> MutableList<E>.listIterator(proc:(MutableListIterator<E>)->Unit)
expect fun <E> MutableList<E>.listIterator(index: Int, proc:(MutableListIterator<E>)->Unit)
expect fun <E> MutableList<E>.subList(fromIndex: Int, toIndex: Int,proc:(MutableList<E>) -> Unit)