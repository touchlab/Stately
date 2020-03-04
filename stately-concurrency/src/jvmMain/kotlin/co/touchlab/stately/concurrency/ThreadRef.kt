package co.touchlab.stately.concurrency

actual class ThreadRef actual constructor() {
  private val threadRef = Thread.currentThread().id

  actual fun same(): Boolean = threadRef == Thread.currentThread().id
}