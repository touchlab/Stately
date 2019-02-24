package co.touchlab.stately.platform

import platform.Foundation.NSThread

actual fun sleep(time: Long) {
  NSThread.sleepForTimeInterval(time.toDouble()/1000.toDouble())
}
