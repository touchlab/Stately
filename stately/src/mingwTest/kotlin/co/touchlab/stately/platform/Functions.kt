package co.touchlab.stately.platform

import platform.windows.Sleep

@ExperimentalUnsignedTypes
actual fun sleep(time: Long) {
  Sleep(time.toUInt())
}
