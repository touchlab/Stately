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

import java.util.concurrent.atomic.AtomicLong

actual class AtomicLong actual constructor(value_: Long) {
    private val atom = AtomicLong(value_)
    actual var value: Long
        get() = atom.get()
        set(value) {
            atom.set(value)
        }

    actual fun increment() {
        atom.incrementAndGet()
    }
    actual fun decrement() {
        atom.decrementAndGet()
    }

    actual fun addAndGet(delta: Int): Long = atom.addAndGet(delta.toLong())
    actual fun compareAndSet(expected: Long, new: Long): Boolean = atom.compareAndSet(expected, new)
}