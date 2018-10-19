package co.touchlab.stately.collections

import co.touchlab.stately.Random
import co.touchlab.stately.concurrency.AtomicInt
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

    @Test
    fun rehash(){
        val m = SharedHashMap<String, MapData>()

        val random: Random = Random()
        for(i in 0 until 100_000) {
            val rand = random.nextInt()
            assertEquals(m.rehash(rand), m.rehash(rand))
        }
    }

    @Test
    fun testBasicThreads(){
        val WORKERS = 10
        val LOOP_INSERT = 200
        val LOOP_REMOVE = 15

        val workers = Array(WORKERS){
            MPWorker()
        }

        val m = SharedHashMap<String, MapData>().mpfreeze()

        val count = AtomicInt(0).mpfreeze()

        val start = currentTimeMillis()

        workers.forEach {
            val mycount = count.value

            it.runBackground {
                for(i in 0 until LOOP_INSERT)
                {
                    val key = "W:${mycount} I:$i"
                    m.put(key, MapData("W2:${mycount} I2:$i"))
                }
                for(i in 0 until LOOP_REMOVE)
                {
                    m.remove("W:${mycount} I:${i*10}")
                }
            }

            count.increment()
        }
        workers.forEach { it.requestTermination() }

        println("basic threads time: ${currentTimeMillis() - start}")
        val size = (LOOP_INSERT - LOOP_REMOVE) * WORKERS
        assertEquals(m.size, size)
        assertEquals(m.entries.size, size)
        assertEquals(m.keys.size, size)
        m.clear()
        assertEquals(m.size, 0)
        assertEquals(m.entries.size, 0)
        assertEquals(m.keys.size, 0)
    }

    @Test
    fun testResize(){
        val m = makeCount(10) as SharedHashMap<String, MapData>
        assertEquals(m.currentBucketSize(), 16)
        addCount(m, 14)
        assertEquals(m.currentBucketSize(), 32)
        addCount(m, 28)
        assertEquals(m.currentBucketSize(), 64)
        addCount(m, 60)
        assertEquals(m.currentBucketSize(), 128)
        addCount(m, 118)
        assertEquals(m.currentBucketSize(), 256)
        addCount(m, 230)
        assertEquals(m.currentBucketSize(), 512)

        for(i in 0 until 230){
            assertEquals(m.get("Key: $i"), MapData("Value: $i"))
        }
    }

    @Test
    fun testNotMutableSets(){
        val m = SharedHashMap<String, MapData>()
        for(i in 0 until 15){
            val thediv = i / 3
            m.put("Key: $i", MapData("Value: $thediv"))
        }

        checkImmutableSet(m.keys, 15, 15, "Asdf")
        checkImmutableSet(m.values, 15, 5, MapData("Qwert"))
        checkImmutableSet(m.entries, 15, 15, SharedHashMap.Entry("Foo", MapData("Bar")))
    }

    @Test
    fun testBasicTimes(){
        val start = currentTimeMillis()
        val size = 150_000
        val l = Array(size){ i->
                Pair("Key: $i", MapData("Value: $i")).mpfreeze()
        }

        val keys = Array(size){"Key: $it".mpfreeze()}

        println("TIMETRACE Time setup: ${currentTimeMillis() - start}")

        println("TIMETRACE Time shared: "+ runMutableMapTimes(l, SharedHashMap(keys.size), keys))
        println("TIMETRACE Time reg: "+ runMutableMapTimes(l, HashMap(keys.size), keys))
    }

    fun runMutableMapTimes(l:Array<Pair<String, MapData>>, m:MutableMap<String, MapData>, keys:Array<String>):Long{
        val start = currentTimeMillis()

        l.forEach {
            m.put(it.first, it.second)
        }

        keys.forEach {
            m.get(it)
        }

        return currentTimeMillis() - start
    }

    private fun <T> checkImmutableSet(c:MutableCollection<T>, total:Int, uniques:Int, sample:T){
        assertEquals(c.size, total)
        val checkSet = HashSet<T>()
        c.forEach { checkSet.add(it) }
        assertEquals(checkSet.size, uniques)
        unfail { c.add(sample) }
        unfail { c.addAll(listOf(sample)) }
        unfail { c.clear() }
        unfail { c.remove(sample) }
        unfail { c.removeAll(listOf(sample)) }
        unfail { c.retainAll(listOf(sample)) }
    }

    private fun unfail(proc:()->Unit){
        try {
            proc()
            fail("Call should have failed")
        } catch (e: Exception) {
        }
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