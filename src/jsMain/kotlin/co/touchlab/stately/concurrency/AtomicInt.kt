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
 * Multiplatform AtomicInt implementation
 */
actual class AtomicInt actual constructor(value_: Int) {
    actual var value: Int = value_

    actual fun increment() {
        value++
    }

    actual fun decrement() {
        value--
    }

    actual fun addAndGet(delta: Int): Int {
        value += delta
        return value
    }

    actual fun compareAndSet(expected: Int, new: Int): Boolean {
        return if (expected == value) {
            value = new
            true
        } else {
            false
        }
    }
}