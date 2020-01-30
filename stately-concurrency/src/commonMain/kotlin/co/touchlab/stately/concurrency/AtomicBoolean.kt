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
 * Multiplatform AtomicBoolean implementation
 */
class AtomicBoolean(value_: Boolean) {
  private val atom = AtomicInt(boolToInt(value_))
  var value: Boolean
    get() = atom.value != 0
    set(value) {
      atom.value = boolToInt(value)
    }

  fun compareAndSet(expected: Boolean, new: Boolean): Boolean =
    atom.compareAndSet(boolToInt(expected), boolToInt(new))

  private fun boolToInt(b: Boolean): Int = if (b) {
    1
  } else {
    0
  }
}
