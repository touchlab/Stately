package co.touchlab.stately.concurrency

actual class ThreadRef actual constructor() {
  actual fun reset() {}
  actual fun same(): Boolean = true
}