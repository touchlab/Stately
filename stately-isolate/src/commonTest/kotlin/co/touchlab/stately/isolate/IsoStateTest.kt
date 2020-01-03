package co.touchlab.stately.isolate

import co.touchlab.stately.ensureNeverFrozen
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertFails

class IsoStateTest {
    @Test
    fun basicTest() = runBlocking {
        val map = IsoMap<String, SomeData>()
        repeat(100) { outer ->
            val lmap = mutableMapOf<String, SomeData>()
            val base = outer * 1_000
            repeat(1_000) {
                val num = it + base
                lmap.put("key $num", SomeData("val $num"))
            }

            map.putMap(lmap)
        }

        println("Added, total ${map.size()}")
    }

    @Test
    fun isolatedProducer() {
        assertFails {
            val map = mutableMapOf<String, String>()
            createStateBlocking { map }
        }
    }

//    @Test
    fun noLeakingState():Unit = runBlocking {
        val ls = LeakyState()
        assertFails {
            ls.leak()
        }
        Unit
    }

    @Test
    fun freezeFail() = runBlocking {
        assertFails {
            returnState()
        }
        Unit
    }

    suspend fun returnState() = withContext(stateDispatcher) {
        val sd = SomeData("arst")
        sd.ensureNeverFrozen()
        sd
    }

    class LeakyState: IsolateState<MutableList<String>>({ mutableListOf()}) {
        suspend fun leak() = withContext(stateDispatcher) {
            var l : MutableList<String>? = null
            access { l = it }
            l
        }
    }
}

data class SomeData(val s: String)

class IsoMap<K, V>() : IsolateState<MutableMap<K, V>>({ mutableMapOf() }) {
    suspend fun clear() = withContext(stateDispatcher) {

        access { it.clear() }
    }

    suspend fun size() = withContext(stateDispatcher) {
        access { it.size }
    }

    suspend fun get(mapKey: K): V? = withContext(stateDispatcher) {
        access { it.get(mapKey) }
    }

    suspend fun put(mapKey: K, data: V) = withContext(stateDispatcher) {
        access { it.put(mapKey, data) }
    }

    suspend fun putMap(map: Map<K, V>) = withContext(stateDispatcher) {
        access { it.putAll(map) }
    }
}