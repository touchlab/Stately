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

package co.touchlab.stately.annotation

import co.touchlab.stately.concurrency.AtomicReference
import co.touchlab.stately.isFrozen
import co.touchlab.stately.isNative
import co.touchlab.stately.isNativeFrozen
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AnnotationsTest {
  @Test
  fun testSharedImmutable() {
    assertTrue(topLevel.isNativeFrozen())
  }

  @Test
  fun testThreadLocal() {
    if (isNative) {
      assertFalse(WillBeLocal.isFrozen())
      assertTrue(WontBeLocal.isFrozen())
    }
  }
}

@ThreadLocal
object WillBeLocal {
  val s = "local"
}

object WontBeLocal {
  val s = "not local"
}

@SharedImmutable
val topLevel = TopLevel(AtomicReference("hello"))

data class TopLevel(val atom: AtomicReference<String>)