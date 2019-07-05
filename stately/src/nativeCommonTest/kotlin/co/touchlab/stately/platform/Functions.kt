package co.touchlab.stately.platform

import platform.posix.usleep

actual fun sleep(time: Long) {
    usleep(time.toUInt() * 1000u)
}