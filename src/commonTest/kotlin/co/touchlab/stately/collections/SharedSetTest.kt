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

package co.touchlab.stately.collections

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SharedSetTest {

    @Test
    fun dataClassEquals(){
        val set = frozenHashSet<CollectionValues>()
        assertEquals(0, set.size)
        set.add(CollectionValues("asdf", 123))
        assertEquals(1, set.size)

        set.add(CollectionValues("asdf", 123))
        assertEquals(1, set.size)

        set.add(CollectionValues("asdf", 124))
        assertEquals(2, set.size)
    }

    data class CollectionValues(val a:String, val b:Int)

    /*@Test
    fun testLambdas(){
        val set = SharedSet<()->Unit>()

        val a: () -> Unit = { 1 + 2 }
        val b: () -> Unit = { 1 + 2 }
        val c: () -> Unit = { 2 + 2 }

        assertEquals(a, b)
        assertNotEquals(a, c)

        set.add(a)
        set.add(b)
        assertEquals(1, set.size)

        set.add(c)
        assertEquals(2, set.size)
    }*/
}