package co.touchlab.stately.concurrency

import kotlin.native.concurrent.AtomicInt

@ThreadLocal
private var localThreadId: Int = 0

@SharedImmutable
private val threadIdCounter = AtomicInt(1)

private fun currentThreadId():Int{
  if (localThreadId == 0) {
    localThreadId = threadIdCounter.addAndGet(1)
  }
  return localThreadId
}

actual class ThreadRef actual constructor() {
    private val threadId: Int = currentThreadId()

    actual fun same(): Boolean = threadId == currentThreadId()
}