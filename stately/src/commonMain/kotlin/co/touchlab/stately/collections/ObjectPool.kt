package co.touchlab.stately.collections

import co.touchlab.stately.concurrency.AtomicInt
import co.touchlab.stately.concurrency.AtomicReference
import co.touchlab.stately.concurrency.Lock
import co.touchlab.stately.concurrency.value
import co.touchlab.stately.concurrency.withLock
import co.touchlab.stately.freeze
import co.touchlab.stately.isNativeFrozen

class ObjectPool<T>(
  private val maxSize: Int,
  private val createBlock:()->T,
  private val cleanupBlock:(t:T)->Unit = {}) {
  internal val pool = Array<AtomicReference<T?>>(maxSize) {
    AtomicReference(null)
  }

  private val poolIndex = AtomicInt(0)
  private val lock = Lock()

  fun push(t: T): Boolean = lock.withLock {
    if (!t.isNativeFrozen())
      throw IllegalStateException("Object pool entries must be frozen")

    val index = poolIndex.value

    return if (index >= maxSize) {
      cleanupBlock(t)
      false
    } else {
      pool[index].value = t
      poolIndex.incrementAndGet()
      true
    }
  }

  fun pop(): T = lock.withLock {
    val index = poolIndex.value

    val fromPool = if (index <= 0)
      null
    else {
      val ref = pool[poolIndex.decrementAndGet()]
      val t = ref.value
      ref.value = null
      t
    }

    fromPool ?: createBlock().freeze()
  }

  fun clear() = lock.withLock {
    pool.forEach {
      val t = it.value
      if (t != null)
      {
        cleanupBlock(t)
        it.value = null
      }
    }

    poolIndex.value = 0
  }
}