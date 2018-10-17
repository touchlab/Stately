package co.touchlab.stately.collections

import kotlin.test.Test
import kotlin.test.assertEquals

class SharedLruCacheTest{

    @Test
    fun sanityCheck(){
        val collect = ArrayList<MapData>()
        val sc = SharedLruCache<String, MapData>(4) {
            collect.add(it.value)
        }

        addEntries(6, sc)

        checkResults(collect, MapData("Value: 0"), MapData("Value: 1"))
    }

    private fun addEntries(count:Int, cache:SharedLruCache<String, MapData>){
        for(i in 0 until count){
            cache.put("Key: $i", MapData("Value: $i"))
        }
    }

    private fun checkResults(c:List<Any>, vararg elems:Any){
        for (i in 0 until c.size){
            assertEquals(c.get(i), elems[i])
        }

        assertEquals(c.size, elems.size)
    }
}