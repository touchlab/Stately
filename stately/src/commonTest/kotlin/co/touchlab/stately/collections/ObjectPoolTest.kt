package co.touchlab.stately.collections

import co.touchlab.stately.concurrency.AtomicInt
import co.touchlab.stately.concurrency.value
import kotlin.test.Test
import kotlin.test.assertEquals
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

}

data class PoolVal(val s:String)
