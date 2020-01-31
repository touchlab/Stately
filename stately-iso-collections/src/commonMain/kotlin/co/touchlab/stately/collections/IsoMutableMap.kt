package co.touchlab.stately.collections

import co.touchlab.stately.isolate.IsolateState
import co.touchlab.stately.isolate.StateHolder
import co.touchlab.stately.isolate.createState

class IsoMutableMap<K, V>(producer: () -> MutableMap<K, V> = { mutableMapOf() }) :
    IsolateState<MutableMap<K, V>>(createState(producer)), MutableMap<K, V> {
    override val size: Int
        get() = access { it.size }

    override fun containsKey(key: K): Boolean = access { it.containsKey(key) }
    override fun containsValue(value: V): Boolean = access { it.containsValue(value) }
    override fun get(key: K): V? = access { it.get(key) }
    override fun isEmpty(): Boolean = access { it.isEmpty() }
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = access { IsoMutableSet(fork(it.entries)) }
    override val keys: MutableSet<K>
        get() = access { IsoMutableSet(fork(it.keys)) }
    override val values: MutableCollection<V>
        get() = access { IsoMutableCollection(fork(it.values)) }

    override fun clear() = access { it.clear() }
    override fun put(key: K, value: V): V? = access { it.put(key, value) }
    override fun putAll(from: Map<out K, V>) = access { it.putAll(from) }
    override fun remove(key: K): V? = access { it.remove(key) }
}

open class IsoMutableCollection<T> internal constructor(stateHolder: StateHolder<MutableCollection<T>>) :
    IsolateState<MutableCollection<T>>(stateHolder), MutableCollection<T> {
    constructor(producer: () -> MutableCollection<T>) : this(createState(producer))

    override val size: Int
        get() = access { it.size }

    override fun contains(element: T): Boolean = access { it.contains(element) }
    override fun containsAll(elements: Collection<T>): Boolean = access { it.containsAll(elements) }
    override fun isEmpty(): Boolean = access { it.isEmpty() }
    override fun add(element: T): Boolean = access { it.add(element) }
    override fun addAll(elements: Collection<T>): Boolean = access { it.addAll(elements) }
    override fun clear() = access { it.clear() }
    override fun iterator(): MutableIterator<T> = access { IsoMutableIterator(fork(it.iterator())) }
    override fun remove(element: T): Boolean = access { it.remove(element) }
    override fun removeAll(elements: Collection<T>): Boolean = access { it.removeAll(elements) }
    override fun retainAll(elements: Collection<T>): Boolean = access { it.retainAll(elements) }
}

class IsoMutableSet<T> internal constructor(stateHolder: StateHolder<MutableSet<T>>) :
    IsoMutableCollection<T>(stateHolder), MutableSet<T> {
    constructor(producer: () -> MutableSet<T> = { mutableSetOf() }) : this(createState(producer))
}

class IsoMutableIterator<T> internal constructor(stateHolder: StateHolder<MutableIterator<T>>) :
    IsolateState<MutableIterator<T>>(stateHolder), MutableIterator<T> {
    override fun hasNext(): Boolean = access { it.hasNext() }
    override fun next(): T = access { it.next() }
    override fun remove() = access { it.remove() }
}