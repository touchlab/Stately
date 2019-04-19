package co.touchlab.stately.concurrency

import co.touchlab.stately.freeze
import kotlin.native.concurrent.AtomicInt
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
private object Thread {
  val id: Int by lazy { Any().hashCode() }
}

/*
 * This is a re-entrant implementation of Lock.
 * a thread may acquire the lock as many times as the wish.
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
        assert(reenterCount.value == 0) { "Another thread just took a lock that had a non-zero reenter count." }
        true
      }
      else -> false
    }
  }

  private fun spinLock() {
    while (!tryLock()) {}
  }

  private fun spinUnlock(){
    assert(lockedThreadId.value == Thread.id) { "Attempting to unlock from a thread that doesn't have the lock." }

    // Because this is re-entrant we should only unlock if the count is 0.
    if (reenterCount.value > 0) {
      reenterCount.decrement()
    } else {
      lockedThreadId.compareAndSet(Thread.id, 0)
    }
  }
}
