package co.touchlab.stately.isolate

class IsoMutableMap<K, V>() : IsolateState<MutableMap<K, V>>({ mutableMapOf() }), MutableMap<K, V> {
    override val size: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun containsKey(key: K): Boolean = runBlocking {
        access { it.containsKey(key) }
    }

    override fun containsValue(value: V): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun get(key: K): V? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isEmpty(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val keys: MutableSet<K>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val values: MutableCollection<V>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun clear() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun put(key: K, value: V): V? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun putAll(from: Map<out K, V>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun remove(key: K): V? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}