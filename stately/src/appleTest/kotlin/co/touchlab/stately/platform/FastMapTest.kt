package co.touchlab.stately.platform

import co.touchlab.stately.collections.FastNativeHashMap
import co.touchlab.stately.freeze
import kotlin.test.Test
import kotlin.test.assertEquals

class FastMapTest{

    /*@Test
    fun checkStandardBehavior(){
        val map = HashMap<SomeMapKey, SomeMapData>()
        for(i in 0 until 1000){
            val key = SomeMapKey("key $i")
            map[key] = SomeMapData("data $i")
        }

        map.keys.add(SomeMapKey("Should fail"))

        assertEquals(1001, map.size)

        val iterator = map.values.iterator()
        iterator.next()
        iterator.remove()

        assertEquals(1000, map.size)
    }*/

    @Test
    fun memleaks()
    {
        val map = FastNativeHashMap<SomeMapKey, SomeMapData>()
        /*addRef(map)
        println("tempRefCount: ${map.tempRefCount()}")
        val tempVar = map.grabTempVar()
        println("key: $tempVar")
        println("final ${map.refCount(tempVar)}")*/
        for(outer in 0 until 100_000){
            loopMap(map)
        }

        /*var count = 0
        val iter = map.keys.iterator()
        iter.forEach {
            if(count % 100 == 0){
                iter.remove()
            }

            count++
        }

        assertEquals(count, 990)
        assertEquals(map.size, 990)*/
    }

    private fun addRef(map: FastNativeHashMap<SomeMapKey, SomeMapData>){
//        val mapKey = SomeMapKey("crash").freeze()
//        println("refcount before: ${map.refCount(mapKey)}")
        map.setTempVar(makeRef().freeze())
//        println("refcount after: ${map.refCount(mapKey)}")
    }

    private fun makeRef():SomeMapKey= SomeMapKey("fuck")

    private fun loopMap(map: FastNativeHashMap<SomeMapKey, SomeMapData>) {
        for (i in 0 until 10_000) {
            val key = SomeMapKey("key $i")
            val orig = map.put(key, SomeMapData("data $i"))
            if (orig != null && orig.s == "data 555") {
//                print("orig $orig")
            }
        }

    }

    /*@Test
    fun keysAndIterator()
    {
        val map = FastNativeHashMap<SomeMapKey, SomeMapData>()

        for(i in 0 until 1000){
            val key = SomeMapKey("key $i")
            map[key] = SomeMapData("data $i")
        }

        var count = 0
        val iter = map.keys.iterator()
        iter.forEach {
            if(count % 100 == 0){
                iter.remove()
            }

            count++
        }

        assertEquals(count, 990)
        assertEquals(map.size, 990)
    }*/

    /*@Test
    fun runFastMap(){
        runBigMap(FastNativeHashMap())
    }

    @Test
    fun runCustomKey(){
        *//*val map = FastNativeHashMap<SomeMapKey, SomeMapData>()
        for(i in 0 until 1_000_000){
            val key = SomeMapKey("key $i")
            map.put(key, SomeMapData("data $i"))
        }
*//*
        sleep(20_000)
    }*/

//    @Test
//    fun runFrozenMap() {
//        runBigMap(frozenHashMap())
//    }

    fun runBigMap(map: MutableMap<String, SomeMapData>){
        for(i in 0 until 10_000_000){
            map.put("key $i", SomeMapData("data $i"))
        }

        assertEquals(10_000_000, map.size)

        /*for (i in 0 until 1_000_000){
            assertEquals(map.get("key $i"), SomeMapData("data $i"))
        }*/



        map.clear()
    }
}

data class SomeMapKey(val s:String)
data class SomeMapData(val s: String)