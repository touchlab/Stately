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

package co.touchlab.stately

import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HelpersTest{
    @Test
    fun ensureNeverFrozenNoFreezeChild(){
        if(!isNative)
            return

        val noFreeze = Hi("qwert")
        noFreeze.ensureNeverFrozen()

        val nested = Nested(noFreeze)
        assertFails { nested.freeze() }
    }

    @Test
    fun ensureNeverFrozenFailsTarget(){
        if(!isNative)
            return

        val noFreeze = Hi("qwert")
        noFreeze.ensureNeverFrozen()

        assertFalse(noFreeze.isFrozen())
        noFreeze.freeze()
        assertTrue(noFreeze.isFrozen())
    }

    @Test
    fun ensureNeverFrozenFailOnFrozen(){
        if(!isNative)
            return

        val wasFrozen = Hi("qwert").freeze()
        assertFails { wasFrozen.ensureNeverFrozen() }
    }

    data class Hi(val s:String)
    data class Nested(val hi:Hi)
}

