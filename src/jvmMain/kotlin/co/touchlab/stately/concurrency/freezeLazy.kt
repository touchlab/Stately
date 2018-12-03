package co.touchlab.stately.concurrency

public actual fun <T> freezeLazy(initializer: () -> T): Lazy<T> = lazy(initializer)