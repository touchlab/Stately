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

import kotlin.native.concurrent.AtomicLong

actual class AtomicLong actual constructor(initialValue: Long) {
  private val atom = AtomicLong(initialValue)

  actual fun get(): Long = atom.value

  actual fun set(newValue: Long) {
    atom.value = newValue
  }

  actual fun incrementAndGet(): Long = atom.addAndGet(1)

  actual fun decrementAndGet(): Long = atom.addAndGet(-1)

  actual fun addAndGet(delta: Long): Long = atom.addAndGet(delta)

  actual fun compareAndSet(expected: Long, new: Long): Boolean = atom.compareAndSet(expected, new)

}
