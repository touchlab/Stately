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

package co.touchlab.stately.concurrency

/**
 * On JVM this is a java.util.concurrent.Semaphore instance. On anything from apple this is an NSLock. On
 * all other platforms this will currently be a spin lock, as pthread_mutex requires a destructor.
 */
actual class SingleLock actual constructor() : Lock {
    var locked = false
    actual override fun lock() {
        if(locked)
            throw IllegalStateException("Locks are non-reentrant and JS is single threaded")
        locked = true
    }
    actual override fun unlock() {
        locked = false
    }

    actual override fun tryAcquire(): Boolean = !locked
}