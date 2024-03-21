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
 * Multiplatform AtomicLong implementation
 */
actual class AtomicLong actual constructor(initialValue: Long) {
    private var internalValue: Long = initialValue

    actual fun addAndGet(delta: Long): Long {
        return (internalValue + delta).also {
            internalValue = it
        }
    }

    actual fun compareAndSet(expected: Long, new: Long): Boolean {
        return if (expected == internalValue) {
            internalValue = new
            true
        } else {
            false
        }
    }

    actual fun get(): Long = internalValue

    actual fun set(newValue: Long) {
        internalValue = newValue
    }

    actual fun incrementAndGet(): Long = addAndGet(1)

    actual fun decrementAndGet(): Long = addAndGet(-1)
}
