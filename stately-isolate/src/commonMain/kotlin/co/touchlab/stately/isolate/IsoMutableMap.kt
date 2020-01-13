package co.touchlab.stately.isolate

import co.touchlab.stately.ensureNeverFrozen
import co.touchlab.stately.isFrozen
import co.touchlab.stately.isNativeFrozen

class IsoMutableMap<K, V> : IsolateState<MutableMap<K, V>>({ mutableMapOf() }), MutableMap<K, V> {

    init {
        println("Ensure Never Frozen")
        if(isNativeFrozen()){
            println("Natively Frozen")
        }
        if(isFrozen()){
            println("Frozen")
        }
        ensureNeverFrozen()
    }

    override val size: Int
        get() = runBlocking {
            println("Size")
            access { it.size } }

    override fun containsKey(key: K): Boolean = runBlocking {
        println("Contains Key")
        access { it.containsKey(key)
        }
    }

    override fun containsValue(value: V): Boolean = runBlocking {
        println("Value")
        access { it.containsValue(value)
        }
    }

    override fun get(key: K): V? = runBlocking {
        println("Get")
        access { it[key] }
    }

    override fun isEmpty(): Boolean = runBlocking {
        println("empty")
        access { it.isEmpty()
        }
    }

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() =  runBlocking {
            println("Entries")
            access { it.entries }
        }
    override val keys: MutableSet<K>
        get() =  runBlocking {
            println("Keys")
            access { it.keys }
        }
    override val values: MutableCollection<V>
        get() = runBlocking {
            println("Values")
            access { it.values }
        }

    override fun clear() = runBlocking {
        println("Clear")
        access { it.clear() }
    }

    override fun put(key: K, value: V): V? = runBlocking {
        println("put")
        access { it.put(key,value) }
    }

    override fun putAll(from: Map<out K, V>) = runBlocking {
        println("put all")
        access { it.putAll(from) }
    }

    override fun remove(key: K): V? = runBlocking {
        println("remove")
        access { it.remove(key) }
    }

}