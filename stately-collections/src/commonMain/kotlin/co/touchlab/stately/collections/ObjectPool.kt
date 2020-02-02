package co.touchlab.stately.collections

import co.touchlab.stately.concurrency.AtomicInt
import co.touchlab.stately.concurrency.AtomicReference
import co.touchlab.stately.concurrency.Lock
import co.touchlab.stately.concurrency.value
import co.touchlab.stately.concurrency.withLock
import co.touchlab.stately.freeze

class ObjectPool<T>(
  private val maxSize: Int,
  private val createBlock:()->T,
  private val cleanupBlock:((t:T)->Unit)? = null) {
  init {
    if(maxSize < 0)
      throw IllegalArgumentException("maxSize cannot be negative")
  }

  internal val pool = Array<AtomicReference<T?>>(maxSize) {
    AtomicReference(null)
  }

  private val poolIndex = AtomicInt(0)
  private val lock = Lock()

  fun push(t: T): Boolean = lock.withLock {


    if(maxSize == 0){
      cleanupBlock?.invoke(t)
      false
    } else {
      val index = poolIndex.value

      return if (index >= maxSize) {
        cleanupBlock?.invoke(t)
        false
      } else {
        pool[index].value = t
        poolIndex.incrementAndGet()
        true
      }
    }
  }

  fun pop(): T = lock.withLock {
    if(maxSize == 0){
      createBlock().freeze()
    } else {
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
  }

  fun clear() = lock.withLock {
    pool.forEach {
      val t = it.value
      if (t != null)
      {
        cleanupBlock?.invoke(t)
        it.value = null
      }
    }

    poolIndex.value = 0
  }
}