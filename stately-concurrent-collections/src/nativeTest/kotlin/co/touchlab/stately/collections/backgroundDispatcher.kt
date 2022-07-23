package co.touchlab.stately.collections

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual val backgroundDispatcher: CoroutineDispatcher
    get() = Dispatchers.Default

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
actual annotation class NoJsTest