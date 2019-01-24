/*
 * Copyright (C) 2018 Touchlab, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.touchlab.stately.annotation

import kotlin.reflect.KClass

/**
 * expect/actual for native annotation. Only impacts native code.
 */
@Deprecated("SharedImmutable is now available in common",
    ReplaceWith("kotlin.native.concurrent.SharedImmutable"))
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.BINARY)
expect annotation class SharedImmutable()

/**
 * expect/actual for native annotation. Only impacts native code.
 */
@Deprecated("ThreadLocal is now available in common",
    ReplaceWith("kotlin.native.concurrent.ThreadLocal"))
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
expect annotation class ThreadLocal()

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR)
@Retention(AnnotationRetention.SOURCE)
expect annotation class Throws(vararg val exceptionClasses: KClass<out Throwable>)