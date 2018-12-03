/*
 * Copyright (C) 2018 Touchlab, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.touchlab.stately.collections

import co.touchlab.stately.concurrency.AtomicInt
import co.touchlab.stately.concurrency.value
import co.touchlab.stately.freeze
import co.touchlab.stately.isNative
import co.touchlab.stately.isNativeFrozen
import kotlin.test.*

class SharedLruCacheTest{

    @Test
    fun testInitFrozen(){
        assertTrue(SharedLruCache<String, MapData>(4).isNativeFrozen())
    }

    @Test
    fun sanityCheck(){
        val collect = SharedLinkedList<MapData>()
        val sc = SharedLruCache<String, MapData>(4) {
            collect.add(it.value)
        }

        addEntries(6, sc)

        checkResults(collect, MapData("Value: 0"), MapData("Value: 1"))

        sc.get("Key: 2")

        sc.put("Asdf", MapData("Query"))

        //Key: 2 Should've been bumped to the front...
        assertEquals(collect.get(2), MapData("Value: 3"))
    }

    @Test
    fun put(){
        val collect = SharedLinkedList<MapData>()
        val sc = SharedLruCache<String, MapData>(4) {
            collect.add(it.value)
        }

        addEntries(4, sc)

        assertEquals(collect.size, 0)

        //One more than capacity
        addEntries(1, sc, 4)

        assertEquals(collect.size, 1)
        checkResults(collect, MapData("Value: 0"))

        //Adding duplicate value removes nothing, but bumps '1' to top of book
        val refreshResult = sc.put("Key: 1", MapData("Value: 1"))
        assertNull(refreshResult)
        assertEquals(collect.size, 1)
        checkResults(collect, MapData("Value: 0"))

        addEntries(1, sc, 5)

        checkResults(collect, MapData("Value: 0"), MapData("Value: 2"))
    }

    @Test
    fun remove(){
        val collect = SharedLinkedList<MapData>()
        val sc = SharedLruCache<String, MapData>(4) {
            collect.add(it.value)
        }

        addEntries(4, sc)

        sc.remove("Key: 2")
        sc.remove("Key: 0")
        sc.remove("Key: 5")

        checkResults(collect, MapData("Value: 2"), MapData("Value: 0"))

        assertEquals(sc.size, 2)
        sc.remove("Key: 1")
        sc.remove("Key: 3")

        checkResults(collect,
            MapData("Value: 2"),
            MapData("Value: 0"),
            MapData("Value: 1"),
            MapData("Value: 3")
        )

        assertEquals(sc.size, 0)
    }

    @Test
    fun removeSkip(){
        val collect = SharedLinkedList<MapData>()
        val sc = SharedLruCache<String, MapData>(4) {
            collect.add(it.value)
        }

        addEntries(4, sc)

        assertEquals(MapData("Value: 2"), sc.remove("Key: 2", skipCallback = true))
        assertEquals(MapData("Value: 0"), sc.remove("Key: 0", skipCallback = false))
        assertNull(sc.remove("Key: 5", skipCallback = true))

        assertEquals(1, collect.size)
    }

    @Test
    fun entries() {
        val collect = SharedLinkedList<MapData>()
        val sc = SharedLruCache<String, MapData>(4) {
            collect.add(it.value)
        }

        addEntries(4, sc)

        checkSetEntries(sc.entries,
            MapData("Value: 0"),
            MapData("Value: 1"),
            MapData("Value: 2"),
            MapData("Value: 3")
        )

        sc.remove("Key: 1")
        sc.remove("Key: 123")

        sc.get("Key: 2")
        sc.get("Key: 0")
        sc.put("a", MapData("1"))

        checkSetEntries(sc.entries,
            MapData("Value: 3"),
            MapData("Value: 2"),
            MapData("Value: 0"),
            MapData("1")
        )

        sc.put("b", MapData("2"))

        checkSetEntries(sc.entries,
            MapData("Value: 2"),
            MapData("Value: 0"),
            MapData("1"),
            MapData("2")
        )
    }

    @Test
    fun removeAll(){
        val collect = SharedLinkedList<MapData>()
        val sc = SharedLruCache<String, MapData>(4) {
            collect.add(it.value)
        }

        addEntries(4, sc)

        sc.removeAll()

        assertEquals(sc.size, 0)
        assertEquals(collect.size, 4)

        addEntries(4, sc)

        assertEquals(sc.size, 4)
        collect.clear()
        assertEquals(collect.size, 0)

        sc.removeAll(skipCallback = true)
        assertEquals(sc.size, 0)
        assertEquals(collect.size, 0)
    }

    @Test
    fun get(){
        val collect = SharedLinkedList<MapData>()
        val sc = SharedLruCache<String, MapData>(4) {
            collect.add(it.value)
        }

        addEntries(4, sc)

        assertEquals(collect.size, 0)

        sc.put("a", MapData("1"))
        assertEquals(collect.size, 1)

        assertEquals(sc.get("Key: 1"), MapData("Value: 1"))
        assertNull(sc.get("Key: 11"))

        sc.put("b", MapData("2"))

        checkResults(collect, MapData("Value: 0"), MapData("Value: 2"))
    }

    /**
     * onRemove can be called from any thread, so the callback must be frozen
     */
    @Test
    fun onRemoveCanFreeze(){
        //Native only issue
        if(!isNative)
            return

        val collect = ArrayList<MapData>()
        val sc = SharedLruCache<String, MapData>(2) {
            collect.add(it.value)
        }

        sc.put("a", MapData("1"))
        sc.put("b", MapData("1"))
        assertFails {
            sc.put("c", MapData("1"))
        }

    }

    @Test
    fun exists(){
        val sc = SharedLruCache<String, MapData>(4)
        addEntries(4, sc)

        checkExists(sc, "Key: 0", "Key: 1", "Key: 2", "Key: 3")

        sc.put("1", MapData("a"))

        checkExists(sc, "Key: 1", "Key: 2", "Key: 3", "1")

        sc.get("Key: 1")
        sc.put("2", MapData("b"))

        checkExists(sc, "Key: 1", "Key: 3", "1", "2")

        sc.remove("Key: 3")

        checkExists(sc, "Key: 1", "1", "2")

        sc.removeAll(skipCallback = true)

        checkExists(sc)
    }

    /*@Test
    fun initFrozen(){
        val sc = SharedLruCache<String, MapData>(4)
        assertTrue(sc.isNativeFrozen())
    }*/

    @Test
    fun stress(){
        val MAX_CACHE_SIZE = 4
        val sc = SharedLruCache<String, MapData>(MAX_CACHE_SIZE).freeze()

        sc.put("key 1", MapData("a"))
        sc.put("key 2", MapData("a"))
        sc.put("key 3", MapData("a"))
        sc.put("key 4", MapData("a"))

        val stopTime = currentTimeMillis() + (15*1000)

        val worker = MPWorker()
        worker.runBackground {
            var count = 5
            while (currentTimeMillis() < stopTime){
                sc.put("key $count", MapData("val $count"))
                count++
            }
        }

        worker.runBackground {
            var count = 500_000
            while (currentTimeMillis() < stopTime){
                sc.put("key $count", MapData("val $count"))
                count++
            }
        }

        while (currentTimeMillis() < stopTime){
            if(sc.size != 4)
            {
                sc.printDebug()
            }
            assertEquals(4, sc.size)
        }
    }

    @Test
    fun mtPutStress(){
        val CACHE_SIZE = 100
        val LOOPS = 5000

        val count = AtomicInt(0)
        val ops = ThreadOps<SharedLruCache<String, MapData>> {
            SharedLruCache(CACHE_SIZE){count.incrementAndGet()}
        }

        for(i in 0 until LOOPS){
            ops.exe {
                it.put("key $i", MapData("data $i"))
            }
        }

        val lru = ops.run(threads = 8, randomize = true)
        assertEquals(CACHE_SIZE, lru.size)
        assertEquals(LOOPS - CACHE_SIZE, count.value)
    }

    private fun checkExists(lru:LruCache<String, MapData>, vararg keys:String){
        keys.forEach { assertTrue { lru.exists(it) } }
        assertEquals(lru.size, keys.size)
    }

    private fun checkSetEntries(c:Set<MutableMap.MutableEntry<String, MapData>>, vararg elems:MapData){
        val checkSet = HashSet<MapData>()
        c.forEach { checkSet.add(it.value) }
        for(i in 0 until elems.size){
            assertTrue(checkSet.contains(elems[i]))
        }
        assertEquals(c.size, elems.size)
    }

    private fun addEntries(count:Int, cache:SharedLruCache<String, MapData>, startAt:Int = 0){

        for(i in 0 until count){
            val insertIndex = i + startAt
            cache.put("Key: $insertIndex", MapData("Value: $insertIndex"))
        }
    }

    private fun checkResults(c:List<Any>, vararg elems:Any){
        for (i in 0 until c.size){
            assertEquals(c.get(i), elems[i])
        }

        assertEquals(c.size, elems.size)
    }
}