package co.touchlab.stately.concurrency

public expect fun <T> freezeLazy(initializer: () -> T): Lazy<T>