package co.touchlab.stately

import platform.Foundation.NSLock

actual typealias Lock = NSLock

/**
 * For pthread_mutex, which we're not using yet, but will be.
 */
actual fun Lock.close() {}

