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

import co.touchlab.stately.collections.frozenLinkedList
import co.touchlab.testhelp.concurrency.ThreadOperations
import co.touchlab.testhelp.concurrency.currentTimeMillis
import co.touchlab.testhelp.concurrency.sleep
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Just testing that the locks do their basic thing. Not testing reentrant/single.
 */
class Locks {
  @Test
  fun spinLockWorks() {
    val lock = Lock()
    val ops = ThreadOperations { }

    val times = frozenLinkedList<Long>()
    val start = currentTimeMillis()

    val THREADS = 3

    for (i in 0 until THREADS)
      ops.exe {
        lock.withLock {
          sleep(1000)
          times.add(currentTimeMillis())
        }
      }

    ops.run(THREADS)

    var lastVal = start

    times.forEach {
      val diff = it - lastVal
      //Checks that each run of the loop is significantly delayed, but
      //giving it a pretty wide range. Used to be +/- 100 ms.
      //Have had CI failures presumably because
      //the machines are more "distracted" than my laptop.
      assertTrue { diff in 500..1500 }
      lastVal = it
    }
  }
}