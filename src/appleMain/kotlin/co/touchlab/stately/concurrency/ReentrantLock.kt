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

import platform.Foundation.NSRecursiveLock

actual class ReentrantLock actual constructor() : Lock {
    private val recursiveLock = NSRecursiveLock()

    actual override fun lock() {
        recursiveLock.lock()
    }

    actual override fun unlock() {
        recursiveLock.unlock()
    }

    actual override fun tryAcquire(): Boolean = recursiveLock.tryLock()
}