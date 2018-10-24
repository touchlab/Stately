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

package prototypes

import kotlin.test.Test

class DetachedTests{

    /**
     * Uncomment @Test if you want to see some timing tests
     */
//    @Test
    fun arrayListTimeTest(){
//        for(i in 1..30){
//            makeListSizeOf(10000 * i)
//        }
        makeListSizeOf(10000)
//        makeListSizeOf(20000)
//        makeListSizeOf(30000)
//        makeListSizeOf(40000)
        makeListSizeOf(50000)
        makeListSizeOf(100000)
    }
}

expect fun makeListSizeOf(size:Int)