package co.touchlab.stately.annotation

/*
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.BINARY)
actual annotation class SharedImmutable actual constructor()
*/

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.BINARY)
actual
annotation class SharedImmutable

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
actual
annotation class ThreadLocal

