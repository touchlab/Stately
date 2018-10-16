package co.touchlab.stately.collections

import kotlin.test.*

class SharedHashMapTest{
    @Test
    fun put(){
        val m = SharedHashMap<String, MapData>()
        m.put("asdf", MapData("qwert"))

        assertEquals(MapData("qwert"), m.get("asdf"))

        assertEquals(MapData("qwert"), m.put("asdf", MapData("rrr")))

        assertEquals(MapData("rrr"), m.get("asdf"))
        assertEquals(1, m.size)
        m.put("qqq", MapData("asdf"))
        assertEquals(2, m.size)
    }

    @Test
    fun remove(){
        val m = SharedHashMap<String, MapData>()
        addTen(m)
        assertEquals(10, m.size)
        assertEquals(MapData("Value: 8"), m.remove("Key: 8"))
        assertNull(m.remove("asdf"))
        assertNull(m.remove("Key: 8"))
        assertEquals(9, m.size)
    }

    @Test
    fun putAll(){
        val m = SharedHashMap<String, MapData>()
        addTen(m)

        val madd = SharedHashMap<String, MapData>()
        madd.put("Key: 5", MapData("Value: 5b"))
        madd.put("Key: 10", MapData("Value: 10"))

        m.putAll(madd)

        assertEquals(11, m.size)
        assertEquals(m.get("Key: 5"), MapData("Value: 5b"))
    }

    @Test
    fun isEmpty(){
        val m = SharedHashMap<String, MapData>()
        addTen(m)

        assertFalse(m.isEmpty())
        m.clear()
        assertTrue(m.isEmpty())
        addTen(m)
        m.entries.forEach {
            m.remove(it.key)
        }
        assertTrue(m.isEmpty())
    }

    @Test
    fun containsKey(){
        val m = SharedHashMap<String, MapData>()
        addTen(m)

        assertTrue(m.containsKey("Key: 3"))
        assertFalse(m.containsKey("Key: 10"))
    }

    @Test
    fun containsValue(){
        val m = SharedHashMap<String, MapData>()
        addTen(m)

        assertTrue(m.containsValue(MapData("Value: 4")))
        assertFalse(m.containsValue(MapData("Value: 10")))
    }

    @Test
    fun keys(){
        val m = makeCount(3)
        assertTrue(checkCollection(m.keys, "Key: 0", "Key: 1", "Key: 2"))
    }

    @Test
    fun values(){
        val m = makeCount(3)
        assertTrue(checkCollection(m.values, MapData("Value: 0"), MapData("Value: 1"), MapData("Value: 2")))
    }

    @Test
    fun entries(){
        val m = makeCount(3)
        assertTrue(checkCollection(m.entries,
            SharedHashMap.Entry("Key: 0", MapData("Value: 0")),
            SharedHashMap.Entry("Key: 1", MapData("Value: 1")),
            SharedHashMap.Entry("Key: 2", MapData("Value: 2"))))
    }

    private fun <T> checkCollection(c:Collection<T>, vararg checks:T):Boolean{
        for (check in checks) {
            if(!c.contains(check))
                return false
        }

        return c.size == checks.size
    }

    private fun makeCount(count:Int):MutableMap<String, MapData>{
        val m = SharedHashMap<String, MapData>()
        addCount(m, count)
        return m
    }

    private fun addTen(m:MutableMap<String, MapData>)= addCount(m, 10)

    private fun addCount(m:MutableMap<String, MapData>, count:Int){
        for(i in 0 until count){
            m.put("Key: $i", MapData("Value: $i"))
        }
    }
}

data class MapData(val s:String)