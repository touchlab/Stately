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

import co.touchlab.stately.concurrency.Lock
import kotlin.native.concurrent.AtomicInt
import kotlin.native.concurrent.AtomicReference
import kotlin.native.concurrent.freeze

class CopyOnWriteList<T>(elements: Collection<T>) : MutableList<T> {

    private val listData = AtomicReference<List<T>>(ArrayList(elements).freeze())
    private val instanceLock = Lock()

    init {
        freeze()
    }

    constructor() : this(ArrayList<T>(0))
    constructor(initialCapacity: Int = 0) : this(ArrayList<T>(initialCapacity))

    private inline fun <R> modifyList(proc: (MutableList<T>) -> R): R {
        instanceLock.lock()

        try {
            val mutableList = ArrayList(listData.value)
            val result = proc(mutableList)
            listData.value = mutableList.freeze()

            return result
        } finally {
            instanceLock.unlock()
        }
    }

  /*private inline fun <R> modifyListLockless(proc:(MutableList<T>)->R):R{
      var updated = false
      var result:R? = null
      while (!updated){
          val orig = listData.value
          val mutableList = ArrayList(orig)
          result = proc(mutableList)
          updated = listData.compareAndSet(orig, mutableList.freeze())
      }

      return result!!
  }*/

    override val size: Int
        get() = listData.value.size

    override fun contains(element: T): Boolean = listData.value.contains(element)

    override fun containsAll(elements: Collection<T>): Boolean = listData.value.containsAll(elements)

    override fun get(index: Int): T = listData.value.get(index)

    override fun indexOf(element: T): Int = listData.value.indexOf(element)

    override fun isEmpty(): Boolean = listData.value.isEmpty()

    override fun iterator(): MutableIterator<T> =
        LocalIterator(listData.value)

    override fun lastIndexOf(element: T): Int = listData.value.lastIndexOf(element)

    override fun add(element: T): Boolean = modifyList { it.add(element) }

    override fun add(index: Int, element: T) = modifyList { it.add(index, element) }

    override fun addAll(index: Int, elements: Collection<T>): Boolean = modifyList { it.addAll(index, elements) }

    override fun addAll(elements: Collection<T>): Boolean = modifyList { it.addAll(elements) }

    override fun clear() = modifyList { it.clear() }

    override fun listIterator(): MutableListIterator<T> =
        LocalListIterator(listData.value)

    override fun listIterator(index: Int): MutableListIterator<T> =
        LocalListIterator(listData.value, index)

    override fun remove(element: T): Boolean = modifyList { it.remove(element) }

    override fun removeAll(elements: Collection<T>): Boolean = modifyList { it.removeAll(elements) }

    override fun removeAt(index: Int): T = modifyList { it.removeAt(index) }

    override fun retainAll(elements: Collection<T>): Boolean = modifyList { it.retainAll(elements) }

    override fun set(index: Int, element: T): T = modifyList { it.set(index, element) }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> = modifyList { it.subList(fromIndex, toIndex) }

    private open class LocalIterator<T>(private val list: List<T>, startIndex: Int = 0) : MutableIterator<T> {
        val index = AtomicInt(startIndex)
        override fun hasNext(): Boolean = list.size > index.value

        override fun next(): T = list.get(index.addAndGet(1) - 1)

        override fun remove() {
            throw UnsupportedOperationException("Can't mutate list from iterator")
        }
    }

    private class LocalListIterator<T>(private val list: List<T>, startIndex: Int = 0) :
        LocalIterator<T>(list, startIndex), MutableListIterator<T> {
        override fun hasPrevious(): Boolean = index.value > 0

        override fun nextIndex(): Int = index.value

        override fun previous(): T = list.get(index.addAndGet(-1))

        override fun previousIndex(): Int = index.value - 1

        override fun add(element: T) {
            throw UnsupportedOperationException()
        }

        override fun set(element: T) {
            throw UnsupportedOperationException()
        }
    }
}
