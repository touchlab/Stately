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

import kotlin.native.concurrent.AtomicInt

actual class AtomicInt actual constructor(initialValue:Int){
  private val atom = AtomicInt(initialValue)

  actual fun get(): Int = atom.value

  actual fun set(newValue: Int) {
    atom.value = newValue
  }

  actual fun incrementAndGet(): Int = atom.addAndGet(1)

  actual fun decrementAndGet(): Int = atom.addAndGet(-1)

  actual fun addAndGet(delta: Int): Int = atom.addAndGet(delta)

  actual fun compareAndSet(expected: Int, new: Int): Boolean = atom.compareAndSet(expected, new)

}