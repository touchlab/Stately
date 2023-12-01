package co.touchlab.stately.collections

import co.touchlab.stately.concurrency.Synchronizable
import co.touchlab.stately.concurrency.synchronize
import kotlin.jvm.JvmName

class ConcurrentMutableMap<K, V> internal constructor(
    rootArg: Synchronizable? = null,
    private val del: MutableMap<K, V>
) : Synchronizable(), MutableMap<K, V> {

    constructor() : this(null, mutableMapOf())

    private val syncTarget: Synchronizable = rootArg ?: this

    override val size: Int
        get() = syncTarget.synchronize { del.size }
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = syncTarget.synchronize { ConcurrentMutableSet(this, del.entries) }
    override val keys: MutableSet<K>
        get() = syncTarget.synchronize { ConcurrentMutableSet(this, del.keys) }
    override val values: MutableCollection<V>
        get() = syncTarget.synchronize { ConcurrentMutableCollection(this, del.values) }

    override fun containsKey(key: K): Boolean = syncTarget.synchronize { del.containsKey(key) }
    override fun containsValue(value: V): Boolean = syncTarget.synchronize { del.containsValue(value) }
    override fun get(key: K): V? = syncTarget.synchronize { del.get(key) }
    override fun isEmpty(): Boolean = syncTarget.synchronize { del.isEmpty() }
    override fun clear() {
        syncTarget.synchronize { del.clear() }
    }

    /**
     * If the specified key is not already associated with a value
     * attempts to compute its value using the given mapping function and enters it into this map
     */
    @JvmName("safeComputeIfAbsent")
    fun computeIfAbsent(key: K, defaultValue: (K) -> V): V {
        return syncTarget.synchronize {
            val value = del[key]
            if (value == null) {
                val newValue = defaultValue(key)
                del[key] = newValue
                newValue
            } else {
                value
            }
        }
    }

    override fun put(key: K, value: V): V? = syncTarget.synchronize { del.put(key, value) }
    override fun putAll(from: Map<out K, V>) {
        syncTarget.synchronize { del.putAll(from) }
    }

    override fun remove(key: K): V? = syncTarget.synchronize { del.remove(key) }

    fun <R> block(f: (MutableMap<K, V>) -> R): R = syncTarget.synchronize {
        val wrapper = MutableMapWrapper(del)
        val result = f(wrapper)
        wrapper.map = mutableMapOf()
        result
    }
}

internal class ConcurrentMutableListIterator<E>(
    private val root: Synchronizable,
    private val del: MutableListIterator<E>
) :
    ConcurrentMutableIterator<E>(root, del),
    MutableListIterator<E> {
    override fun hasPrevious(): Boolean = root.synchronize { del.hasPrevious() }

    override fun nextIndex(): Int = root.synchronize { del.nextIndex() }

    override fun previous(): E = root.synchronize { del.previous() }

    override fun previousIndex(): Int = root.synchronize { del.previousIndex() }

    override fun add(element: E) {
        root.synchronize { del.add(element) }
    }

    override fun set(element: E) {
        root.synchronize { del.set(element) }
    }
}

internal class MutableMapWrapper<K, V>(internal var map: MutableMap<K, V>) : MutableMap<K, V> {
    override val size: Int
        get() = map.size

    override fun containsKey(key: K): Boolean = map.containsKey(key)

    override fun containsValue(value: V): Boolean = map.containsValue(value)

    override fun get(key: K): V? = map.get(key)

    override fun isEmpty(): Boolean = map.isEmpty()

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = map.entries
    override val keys: MutableSet<K>
        get() = map.keys
    override val values: MutableCollection<V>
        get() = map.values

    override fun clear() {
        map.clear()
    }

    override fun put(key: K, value: V): V? = map.put(key, value)

    override fun putAll(from: Map<out K, V>) {
        map.putAll(from)
    }

    override fun remove(key: K): V? = map.remove(key)
}