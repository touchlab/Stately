package co.touchlab.stately.collections

import co.touchlab.stately.concurrency.AtomicInt
import co.touchlab.stately.concurrency.value
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse

class ObjectPoolTest {
  @Test
  fun pushMax(){
    var poolCount = 0
    val pool = ObjectPool(20, { PoolVal("arst ${poolCount++}") })
    val poolVals = Array(25){
      pool.pop()
    }

    poolVals.forEach { pool.push(it) }

    assertEquals(20, pool.pool.size)
    assertFalse (pool.pool.any { it.value == null })
  }

  @Test
  fun cleanUpBlock(){
    var poolCount = 0
    val cleanCount = AtomicInt(0)

    val pool = ObjectPool(20, { PoolVal("arst ${poolCount++}") }){
      cleanCount.incrementAndGet()
    }

    val poolVals = Array(25){
      pool.pop()
    }

    poolVals.forEach { pool.push(it) }

    assertEquals(5, cleanCount.value)
    pool.clear()
    assertEquals(25, cleanCount.value)
  }

  @Test
  fun noNegativeSize(){
    assertFails { ObjectPool(maxSize = -2, createBlock = {PoolVal("arst")}) }
  }

  @Test
  fun noCacheWorks(){
    val dumpedList = ArrayList<PoolVal>()
    val createdList = ArrayList<PoolVal>()
    val pool = ObjectPool<PoolVal>(0, { PoolVal("arst") }, {dumpedList.add(it)})
    for (i in 0 until 10){
      createdList.add(pool.pop())
    }

    createdList.forEach {
      assertFalse (pool.push(it))
    }

    assertEquals(10, dumpedList.size)
  }
}

data class PoolVal(val s:String)
