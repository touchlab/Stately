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

import co.touchlab.stately.collections.ListData
import co.touchlab.stately.collections.SharedLinkedList
import co.touchlab.stately.collections.frozenCopyOnWriteList
import co.touchlab.stately.collections.frozenLinkedList
import platform.Foundation.NSLock
import kotlin.native.concurrent.*
import kotlin.system.getTimeMillis

//actual fun makeListSizeOf(size: Int) {}


fun detachState():DetachedObjectGraph<ListData>{
    val data = ListData("asdf")
    return DetachedObjectGraph(TransferMode.SAFE) { data }
}


actual fun makeListSizeOf(size:Int){

//    detachState()
    makeListSizeOf(size, SharedMutableList(), "SharedMutableList")
    makeListSizeOf(size, frozenLinkedList(), "SharedLinkedList")
    makeListSizeOf(size, frozenLinkedList(), "SharedLinkedList")

    makeListSizeOf(size, frozenLinkedList(stableIterator = true), "SharedLinkedList(Stable)")
//    makeListSizeOf(size, frozenCopyOnWriteList(), "frozenCopyOnWriteList")
    makeListSizeOf(size, ArrayList(), "ArrayList")
}

fun makeListSizeOf(size:Int, list:MutableList<ListData>, prefix:String){
    val start = getTimeMillis()

    for(i in 0 until size){
        if(i % 1000 == 0)
        {
            if(getTimeMillis()-start > 120000)
            {
                println("\"$prefix\",$size,0")
                return
            }
        }
        list.add(ListData("key $1"))
    }

//    list.iterator().forEach { it.s }

    val end = getTimeMillis()

    val sb = StringBuilder(prefix)

    while (sb.length < 28)
        sb.append(' ')

    println("\"$prefix\",$size,${end-start}")
}

data class SharedState(val s:String)
data class ListState(val t:String, val state:SharedState)

class SharedMutableList<E>():MutableList<E>{
    private val stateBox: AtomicReference<DetachedObjectGraph<Any>> = AtomicReference(
        DetachedObjectGraph(mode = TransferMode.SAFE,
            producer = { mutableListOf<E>() as Any })
    )
    private val lock = NSLock()
    internal fun withLockDetached(proc: (MutableList<E>) -> MutableList<E>) {
        lock.lock()
        try {
            stateBox.value = DetachedObjectGraph(mode = TransferMode.SAFE,
                producer = {
                    val dataList = stateBox.value.attach() as MutableList<E>
                    proc(dataList) as Any
                })
        } finally {
            lock.unlock()
        }
    }
    override val size: Int
        get() {
            var sizeVal = 0
            withLockDetached {
                sizeVal = it.size
                return@withLockDetached it
            }
            return sizeVal
        }
    override fun contains(element: E): Boolean {
        var b = false
        withLockDetached {
            b = it.contains(element)
            return@withLockDetached it
        }
        return b
    }
    override fun containsAll(elements: Collection<E>): Boolean {
        var b = false
        withLockDetached {
            b = it.containsAll(elements)
            return@withLockDetached it
        }
        return b
    }
    override fun get(index: Int): E {
        var b : E? = null
        withLockDetached {
            b = it.get(index)
            return@withLockDetached it
        }
        return b!!
    }
    override fun indexOf(element: E): Int {
        var b = 0
        withLockDetached {
            b = it.indexOf(element)
            return@withLockDetached it
        }
        return b
    }
    override fun isEmpty(): Boolean {
        var b = false
        withLockDetached {
            b = it.isEmpty()
            return@withLockDetached it
        }
        return b
    }
    override fun iterator(): MutableIterator<E> {
        val outList = ArrayList<E>()
        withLockDetached {
            outList.addAll(it)
            it
        }

        return outList.iterator()
    }
    override fun lastIndexOf(element: E): Int {
        var b = 0
        withLockDetached {
            b = it.lastIndexOf(element)
            return@withLockDetached it
        }
        return b
    }
    override fun add(element: E): Boolean {
        var b = false
        element.freeze()
        withLockDetached {
            b = it.add(element)
            return@withLockDetached it
        }
        return b
    }
    override fun add(index: Int, element: E) {
        element.freeze()
        withLockDetached {
            it.add(index, element)
            return@withLockDetached it
        }
    }
    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        var b = false
        elements.forEach { it.freeze() }
        withLockDetached {
            b = it.addAll(index, elements)
            return@withLockDetached it
        }
        return b
    }
    override fun addAll(elements: Collection<E>): Boolean {
        var b = false
        elements.forEach { it.freeze() }
        withLockDetached {
            b = it.addAll(elements)
            return@withLockDetached it
        }
        return b
    }
    override fun clear() {
        withLockDetached {
            it.clear()
            return@withLockDetached it
        }
    }
    override fun listIterator(): MutableListIterator<E> {
        throw UnsupportedOperationException()
    }
    override fun listIterator(index: Int): MutableListIterator<E> {
        throw UnsupportedOperationException()
    }
    override fun remove(element: E): Boolean {
        var b = false
        withLockDetached {
            b = it.remove(element)
            return@withLockDetached it
        }
        return b
    }
    override fun removeAll(elements: Collection<E>): Boolean {
        var b = false
        withLockDetached {
            b = it.removeAll(elements)
            return@withLockDetached it
        }
        return b
    }
    override fun removeAt(index: Int): E {
        var b : E? = null
        withLockDetached {
            b = it.removeAt(index)
            return@withLockDetached it
        }
        return b!!
    }
    override fun retainAll(elements: Collection<E>): Boolean {
        var b = false
        withLockDetached {
            b = it.retainAll(elements)
            return@withLockDetached it
        }
        return b
    }
    override fun set(index: Int, element: E): E {
        var b : E? = null
        element.freeze()
        withLockDetached {
            b = it.set(index, element)
            return@withLockDetached it
        }
        return b!!
    }
    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> {
        throw UnsupportedOperationException()
    }
    fun close(){
        lock.lock()
        try {
            //Once attached, memory should be cleaned up
            stateBox.value.attach()
        } finally {
            lock.unlock()
        }
    }
}