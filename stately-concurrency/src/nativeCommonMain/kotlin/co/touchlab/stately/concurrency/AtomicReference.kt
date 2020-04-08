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

import kotlin.native.concurrent.AtomicReference
import kotlin.native.concurrent.freeze

//actual typealias AtomicReference<T> = AtomicReference<T>
actual class AtomicReference<V> actual constructor(initialValue: V) {
  private val atom = AtomicReference(initialValue.freeze())
  actual fun get(): V = atom.value

  actual fun set(value_: V) {
    atom.value = value_.freeze()
  }

  /**
   * Compare current value with expected and set to new if they're the same. Note, 'compare' is checking
   * the actual object id, not 'equals'.
   */
  actual fun compareAndSet(expected: V, new: V): Boolean = atom.compareAndSet(expected, new.freeze())
}

