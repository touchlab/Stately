package co.touchlab.stately.isolate

class IsoMutableMap<K, V>() : IsolateState<MutableMap<K, V>>({ mutableMapOf() }), MutableMap<K, V> {
    override val size: Int
        get() = blockingAccess { it.size }

    override fun containsKey(key: K): Boolean = blockingAccess { it.containsKey(key) }

    override fun containsValue(value: V): Boolean = blockingAccess { it.containsValue(value) }

    override fun get(key: K): V? = blockingAccess { it.get(key) }

    override fun isEmpty(): Boolean = blockingAccess { it.isEmpty() }

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = blockingAccess { IsoMutableSet(StateHolder(it.entries)) }

    override val keys: MutableSet<K>
        get() = blockingAccess { IsoMutableSet(StateHolder(it.keys)) }

    override val values: MutableCollection<V>
        get() = blockingAccess { IsoMutableCollection(StateHolder(it.values)) }

    override fun clear() = blockingAccess { it.clear() }

    override fun put(key: K, value: V): V? = blockingAccess { it.put(key, value) }

    override fun putAll(from: Map<out K, V>) = blockingAccess { it.putAll(from) }

    override fun remove(key: K): V? = blockingAccess { it.remove(key) }
}

class IsoMutableCollection<T> internal constructor(stateHolder: StateHolder<MutableCollection<T>>) :
    IsolateState<MutableCollection<T>>(stateHolder), MutableCollection<T> {
    override val size: Int
        get() = blockingAccess { it.size }

    override fun contains(element: T): Boolean = blockingAccess { it.contains(element) }

    override fun containsAll(elements: Collection<T>): Boolean = blockingAccess { it.containsAll(elements) }

    override fun isEmpty(): Boolean = blockingAccess { it.isEmpty() }

    override fun add(element: T): Boolean = blockingAccess { it.add(element) }

    override fun addAll(elements: Collection<T>): Boolean = blockingAccess { it.addAll(elements) }

    override fun clear() = blockingAccess { it.clear() }

    override fun iterator(): MutableIterator<T> = blockingAccess { it.iterator() }

    override fun remove(element: T): Boolean = blockingAccess { it.remove(element) }

    override fun removeAll(elements: Collection<T>): Boolean = blockingAccess { it.removeAll(elements) }

    override fun retainAll(elements: Collection<T>): Boolean = blockingAccess { it.retainAll(elements) }
}

class IsoMutableSet<T> internal constructor(stateHolder: StateHolder<MutableSet<T>>) :
    IsolateState<MutableSet<T>>(stateHolder), MutableSet<T> {
    override fun add(element: T): Boolean = blockingAccess {
        it.add(element)
    }

    override fun addAll(elements: Collection<T>): Boolean = blockingAccess { it.addAll(elements) }

    override fun clear() = blockingAccess { it.clear() }

    override fun iterator(): MutableIterator<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun remove(element: T): Boolean = blockingAccess { it.remove(element) }

    override fun removeAll(elements: Collection<T>): Boolean = blockingAccess { it.removeAll(elements) }

    override fun retainAll(elements: Collection<T>): Boolean = blockingAccess { it.retainAll(elements) }

    override val size: Int
        get() = blockingAccess { it.size }

    override fun contains(element: T): Boolean = blockingAccess { it.contains(element) }

    override fun containsAll(elements: Collection<T>): Boolean = blockingAccess { it.containsAll(elements) }

    override fun isEmpty(): Boolean = blockingAccess { it.isEmpty() }

}