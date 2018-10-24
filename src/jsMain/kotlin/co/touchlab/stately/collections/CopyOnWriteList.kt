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

class CopyOnWriteList<T>(elements:Collection<T>):ArrayList<T>(elements), MutableList<T>{
    constructor():this(ArrayList<T>(0))
    constructor(initialCapacity: Int = 0):this(ArrayList<T>(initialCapacity))

    override fun iterator(): MutableIterator<T> {
        return LocalIterator(ArrayList(this))
    }

    override fun listIterator(): MutableListIterator<T> {
        return LocalListIterator(ArrayList(this))
    }

    override fun listIterator(index: Int): MutableListIterator<T> {
        return LocalListIterator(ArrayList(this), index)
    }

    private open class LocalIterator<T>(private val list:List<T>, startIndex:Int = 0):MutableIterator<T>{
        var index = startIndex
        override fun hasNext(): Boolean = list.size > index

        override fun next(): T = list.get(index++)

        override fun remove() {
            throw UnsupportedOperationException("Can't mutate list from iterator")
        }
    }

    private class LocalListIterator<T>(private val list:List<T>, startIndex:Int = 0):
        LocalIterator<T>(list, startIndex), MutableListIterator<T>{
        override fun hasPrevious(): Boolean = index > 0

        override fun nextIndex(): Int = index

        override fun previous(): T = list.get(--index)

        override fun previousIndex(): Int = index - 1

        override fun add(element: T) {
            throw UnsupportedOperationException()
        }

        override fun set(element: T) {
            throw UnsupportedOperationException()
        }
    }
}