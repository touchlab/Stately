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

class SharedSet<T> :MutableSet<T>{
    private val backingMap = frozenHashMap<T, Unit>()

    init {
        freeze()
    }

    override fun add(element: T): Boolean {
        val result = backingMap.containsKey(element)
        backingMap.put(element, Unit)
        return !result
    }

    override fun addAll(elements: Collection<T>): Boolean =
        elements.fold(false) { b, t ->
            val result = backingMap.containsKey(t)
            backingMap.put(t, Unit)
            b || result
        }

    override fun clear() {
        backingMap.clear()
    }

    override fun remove(element: T): Boolean {
        val result = backingMap.containsKey(element)
        backingMap.remove(element)
        return result
    }

    override fun removeAll(elements: Collection<T>): Boolean  =
        elements.fold(false) { b, t ->
            val result = backingMap.containsKey(t)
            backingMap.remove(t)
            b || result
        }

    override fun retainAll(elements: Collection<T>): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override val size: Int
        get() = backingMap.size

    override fun contains(element: T): Boolean = backingMap.containsKey(element)

    override fun containsAll(elements: Collection<T>): Boolean = backingMap.keys.containsAll(elements)

    override fun isEmpty(): Boolean = backingMap.isEmpty()

    override fun iterator(): MutableIterator<T> = backingMap.keys.iterator()
}
