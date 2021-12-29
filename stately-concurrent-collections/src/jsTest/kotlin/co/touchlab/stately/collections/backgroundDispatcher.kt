package co.touchlab.stately.collections

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlin.test.Ignore

actual val backgroundDispatcher: CoroutineDispatcher
    get() = Dispatchers.Main

actual typealias NoJsTest = Ignore