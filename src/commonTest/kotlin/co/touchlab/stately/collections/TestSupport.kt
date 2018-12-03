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

import co.touchlab.stately.freeze

expect class MPWorker(){
    fun <T> runBackground(backJob:()->T):MPFuture<T>
    fun requestTermination()
}

expect class MPFuture<T>{
    fun consume():T
}

fun createWorker():MPWorker = MPWorker()

expect fun sleep(time:Long)

class ThreadOps<C>(val producer:()->C){
    private val exes = mutableListOf<(C)->Unit>()
    private val tests = mutableListOf<(C)->Unit>()
    var lastRunTime = 0L

    fun exe(proc:(C)->Unit){
        exes.add(proc)
    }

    fun test(proc:(C)->Unit){
        tests.add(proc)
    }
    
    fun run(threads:Int, collection:C = producer(), randomize:Boolean = false):C{
        
        if(randomize){
            exes.shuffle()
            tests.shuffle()
        }

        exes.freeze()

        val start = currentTimeMillis()

        val workers= Array(threads){MPWorker()}
        for(i in 0 until exes.size){
            val ex = exes[i]
            workers[i % workers.size]
                .runBackground {
                    ex(collection)
                }
        }
        workers.forEach { it.requestTermination() }

        tests.forEach { it(collection) }

        lastRunTime = currentTimeMillis() - start

        return collection
    }
}

expect fun currentTimeMillis(): Long