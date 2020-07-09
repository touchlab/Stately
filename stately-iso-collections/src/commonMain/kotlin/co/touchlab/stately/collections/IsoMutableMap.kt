package co.touchlab.stately.collections

import co.touchlab.stately.isolate.IsolateState
import co.touchlab.stately.isolate.createState

open class IsoMutableMap<K, V>(producer: () -> MutableMap<K, V> = { mutableMapOf() }) :
    IsolateState<MutableMap<K, V>>(createState(producer)), MutableMap<K, V> {
    override val size: Int
        get() = access { it.size }

    override fun containsKey(key: K): Boolean = access { it.containsKey(key) }
    override fun containsValue(value: V): Boolean = access { it.containsValue(value) }
    override fun get(key: K): V? = access { it.get(key) }
    override fun isEmpty(): Boolean = access { it.isEmpty() }
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() {
            throw UnsupportedOperationException("Can't leak mutable reference")
            // access { IsoMutableSet(fork(it.entries)) }
        }
    override val keys: MutableSet<K>
        get() = access { IsoMutableSet(fork(it.keys)) }
    override val values: MutableCollection<V>
        get() = access { IsoMutableCollection(fork(it.values)) }

    override fun clear() = access { it.clear() }
    override fun put(key: K, value: V): V? = access { it.put(key, value) }
    override fun putAll(from: Map<out K, V>) = access { it.putAll(from) }
    override fun remove(key: K): V? = access { it.remove(key) }

    override fun equals(other: Any?): Boolean {
        return access { it.equals(other) }
    }

    override fun hashCode(): Int {
        return access { it.hashCode() }
    }
}
