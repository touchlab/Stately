package co.touchlab.stately.collections

import kotlin.test.Ignore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual val backgroundDispatcher: CoroutineDispatcher
    get() = Dispatchers.Main

actual typealias NoJsTest = Ignore
