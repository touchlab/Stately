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
 * Good performance for short operations, but can consume lots of cpu if used in the
 * wrong type of place.
 *
 * If you don't know what this is, use @see co.touchlab.stately.concurrency.ReentrantLock
 */
class SpinLock:Lock{
    val atomicLock = AtomicInt(0)

    override fun lock() {
        spinLock()
    }

    override fun unlock() {
        spinUnlock()
    }

    override fun tryAcquire(): Boolean = atomicLock.value == 0

    private fun spinLock(){
        while (!atomicLock.compareAndSet(0, 1)){}
    }

    private fun spinUnlock(){
        if(!atomicLock.compareAndSet(1, 0))
            throw IllegalStateException("Lock must always be 1 in unlock")
    }
}