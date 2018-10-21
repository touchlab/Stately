package co.touchlab.stately.annotation

/**
 * expect/actual for native annotation. Only impacts native code.
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.BINARY)
expect annotation class SharedImmutable()

/**
 * expect/actual for native annotation. Only impacts native code.
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
expect annotation class ThreadLocal()