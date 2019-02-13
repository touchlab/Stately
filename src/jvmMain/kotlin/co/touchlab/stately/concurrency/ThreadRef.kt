package co.touchlab.stately.concurrency

actual class ThreadRef actual constructor() {
  @Volatile
  private var threadRef = Thread.currentThread().id

  actual fun reset() {
    threadRef = Thread.currentThread().id
  }

  actual fun same(): Boolean = threadRef == Thread.currentThread().id
}