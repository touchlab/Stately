package co.touchlab.stately.annotation

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.BINARY)
expect annotation class SharedImmutable()

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
expect annotation class ThreadLocal()