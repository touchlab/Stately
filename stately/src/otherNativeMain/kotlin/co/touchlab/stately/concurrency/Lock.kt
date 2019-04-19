package co.touchlab.stately.concurrency

import kotlin.native.concurrent.AtomicInt
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
private object Thread {
  private val pointer: Any by lazy { Any() }
  val id: Int by lazy { pointer.hashCode() }
}

/*
 * This is a re-entrant implementation of Lock.
 * a thread may acquire the lock as many times as they wish.
 */
actual class Lock actual constructor() {
  private val lockedThreadId = AtomicInt(0)
  private val reenterCount = AtomicInt(0)

  actual fun lock() {
    spinLock()
  }

  actual fun unlock() {
    spinUnlock()
  }

  actual fun tryLock():Boolean {
    return when (lockedThreadId.compareAndSwap(0, Thread.id)) {
      Thread.id -> {
        // We already have the lock, we are just re-entering.
        reenterCount.increment()
        true
      }
      0 -> {
        // Another thread didn't have the lock so we just took it.
        assert(reenterCount.value == 0) { "Attempt to acquire a lock with a non-zero reenter count." }
        true
      }
      else -> false
    }
  }

  private fun spinLock() {
    while (!tryLock()) {}
  }

  private fun spinUnlock(){
    assert(lockedThreadId.value == Thread.id) { "Attempt to unlock from a thread that doesn't own the lock." }

    // Because this is re-entrant we should only unlock if the count is 0.
    if (reenterCount.value > 0) {
      reenterCount.decrement()
    } else {
      lockedThreadId.compareAndSet(Thread.id, 0)
    }
  }
}
