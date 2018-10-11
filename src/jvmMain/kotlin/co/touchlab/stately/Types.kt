package co.touchlab.stately

import java.util.concurrent.locks.ReentrantLock

actual typealias Lock = ReentrantLock

actual fun Lock.close() {}