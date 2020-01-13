package co.touchlab.stately.isolate

class IsoMutableMap<K, V> : IsolateState<MutableMap<K, V>>({ mutableMapOf() }), MutableMap<K, V> {

    override val size: Int
        get() = runBlocking {
            access { it.size }
        }

    override fun containsKey(key: K): Boolean = runBlocking {
        access { it.containsKey(key) }
    }

    override fun containsValue(value: V): Boolean = runBlocking {
        access { it.containsValue(value) }
    }

    override fun get(key: K): V? = runBlocking {
        access { it[key] }
    }

    override fun isEmpty(): Boolean = runBlocking {
        access { it.isEmpty() }
    }

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() =  runBlocking {
            access { it.toMutableMap().entries }
        }
    override val keys: MutableSet<K>
        get() =  runBlocking {
            access { it.toMutableMap().keys }
        }
    override val values: MutableCollection<V>
        get() = runBlocking {
            access { it.toMutableMap().values }
        }

    override fun clear() = runBlocking {
        access { it.clear() }
    }

    override fun put(key: K, value: V): V? = runBlocking {
        access { it.put(key,value) }
    }

    override fun putAll(from: Map<out K, V>) = runBlocking {
        access { it.putAll(from) }
    }

    override fun remove(key: K): V? = runBlocking {
        access { it.remove(key) }
    }

}