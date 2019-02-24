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

import co.touchlab.stately.concurrency.AtomicBoolean
import co.touchlab.stately.concurrency.AtomicInt
import co.touchlab.stately.concurrency.AtomicReference
import co.touchlab.stately.concurrency.Lock
import co.touchlab.stately.concurrency.value
import co.touchlab.stately.freeze

/**
 * Thread safe linked list implementation for Kotlin Multiplatform. This is intended for situations where there may
 * be fairly regular updates and/or relatively large sizes. List iterators are mutable, and if the list changes
 * while iterating, you won't have concurrent modification exceptions, but obviously the state of the list from
 * when you started iterating is not guaranteed.
 */
class SharedLinkedList<T>(objectPoolSize: Int = 5) : AbstractSharedLinkedList<T>(objectPoolSize) {

  internal val version = AtomicInt(0)

  init {
    freeze()
  }

  override fun updated() {
    val newVal = version.incrementAndGet()
    if (newVal == Int.MAX_VALUE)
      version.set(0)
  }

  override fun listIterator(): MutableListIterator<T> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun listIterator(index: Int): MutableListIterator<T> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun iterator(): MutableIterator<T> = LLIterator(this, version.value)

  fun nodeIterator(): MutableIterator<Node<T>> = NodeIterator(this, version.value)

  class NodeIterator<T>(private val ll: SharedLinkedList<T>, private val version: Int) : MutableIterator<Node<T>> {
    override fun remove() {
      throw UnsupportedOperationException()
    }

    var currentNode = ll.head.value

    private fun checkVersion() {
      if (version != ll.version.get())
        throw ConcurrentModificationException()
    }

    override fun hasNext(): Boolean {
      checkVersion()
      return currentNode != null
    }

    override fun next(): Node<T> {
      checkVersion()
      val retval: Node<T> = currentNode!!
      currentNode = currentNode?.next?.value
      return retval
    }
  }

  class LLIterator<T>(private val ll: SharedLinkedList<T>, private val version: Int) : MutableIterator<T> {
    override fun remove() {
      throw UnsupportedOperationException()
    }

    var currentNode = ll.head.value

    private fun checkVersion() {
      if (version != ll.version.get())
        throw ConcurrentModificationException()
    }

    override fun hasNext(): Boolean {
      checkVersion()
      return currentNode?.nodeValue != null
    }

    override fun next(): T {
      checkVersion()
      val retval: T
      retval = currentNode?.nodeValue!!
      currentNode = currentNode?.next?.value
      return retval
    }
  }
}

/**
 * Fairly heavy handed implementation if list sizes are relatively significant. When stable copies are read,
 * if the list has been updated, the list will lock while a copy is made. If the list is updated often,
 * this is better than the simpler CopyOnWriteList, because EVERY update will lock and save, but something
 * to keep in mind. If updating the list while you're iterating isn't a huge problem, use SharedLinkedList
 */
class CopyOnIterateLinkedList<T>(objectPoolSize: Int = 5) : AbstractSharedLinkedList<T>(objectPoolSize) {

  private val updated: AtomicInt
  private val lastList: AtomicReference<MutableList<T>>

  init {
    updated = AtomicInt(0)
    lastList = AtomicReference(mutableListOf<T>().freeze())
    freeze()
  }

  override fun updated() {
    updated.value = 1
  }

  override fun iterator(): MutableIterator<T> = LocalIterator(withLock(false) { checkUpdate() }.iterator())

  override fun listIterator(): MutableListIterator<T> =
    LocalListIterator(withLock(false) { checkUpdate() }.listIterator())

  override fun listIterator(index: Int): MutableListIterator<T> =
    LocalListIterator(withLock(false) { checkUpdate() }.listIterator(index))

  private fun checkUpdate(): MutableList<T> {
    if (updated.value != 0) {
      val newList = ArrayList<T>(sizeCount.value)
      var node = head.value
      var nodeCount = 0
      while (node != null) {
        newList.add(node.nodeValue)
        node = node.next.value
        nodeCount++
      }

      updated.value = 0
      lastList.value = newList.freeze()
    }
    return lastList.value
  }
}

private open class LocalIterator<T>(private val delegate: MutableIterator<T>) : MutableIterator<T> {
  override fun hasNext(): Boolean = delegate.hasNext()

  override fun next(): T = delegate.next()

  override fun remove() {
    throw UnsupportedOperationException("Can't mutate list from iterator")
  }
}

private class LocalListIterator<T>(private val delegate: MutableListIterator<T>) : MutableListIterator<T> {
  override fun hasNext(): Boolean = delegate.hasNext()

  override fun next(): T = delegate.next()

  override fun remove() {
    throw UnsupportedOperationException("Can't mutate list from iterator")
  }

  override fun hasPrevious(): Boolean = delegate.hasPrevious()

  override fun nextIndex(): Int = delegate.nextIndex()

  override fun previous(): T = delegate.previous()

  override fun previousIndex(): Int = delegate.previousIndex()

  override fun add(element: T) {
    throw UnsupportedOperationException()
  }

  override fun set(element: T) {
    throw UnsupportedOperationException()
  }
}

abstract class AbstractSharedLinkedList<T>(objectPoolSize:Int) : MutableList<T> {
  override fun lastIndexOf(element: T): Int = withLock(false) {
    var lastIndex = -1
    var indexCounter = 0

    var currentNode = head.value

    while (currentNode != null) {
      if (currentNode.nodeValue == element)
        lastIndex = indexCounter

      indexCounter++
      currentNode = currentNode.next.value
    }

    lastIndex
  }

  override fun retainAll(elements: Collection<T>): Boolean = withLock(true) {
    val result: ArrayList<T> = ArrayList<T>(sizeCount.value)

    var currentNode = head.value

    while (currentNode != null) {
      val entry = currentNode.nodeValue
      if (elements.contains(entry))
        result.add(entry)

      currentNode = currentNode.next.value
    }

    internalClear()
    result.forEach {
      internalAdd(makeNode(it))
    }

    true
  }

  override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> {
    throw UnsupportedOperationException()
  }

  override val size: Int
    get() = withLock(false) { sizeCount.value }

  override fun add(element: T): Boolean = withLock(true) {
    return internalAdd(makeNode(element))
  }

  override fun add(index: Int, element: T) {
    withLock(true) {
      if (index == sizeCount.value) {
        internalAdd(makeNode(element))
      } else {
        internalNodeAt(index).internalAdd(element)
      }
    }
  }

  fun addNode(element: T): Node<T> = withLock(true) {
    val node = makeNode(element)
    internalAdd(node)
    return node
  }

  override fun addAll(index: Int, elements: Collection<T>): Boolean = withLock(true) {
    return when {
      index == sizeCount.value -> internalAddAll(elements)
      index > sizeCount.value -> throw IndexOutOfBoundsException("Index $index > ${sizeCount.value}")
      else -> {
        val node = internalNodeAt(index)
        elements.forEach {
          node.internalAdd(it)
        }
        true
      }
    }
  }

  override fun addAll(elements: Collection<T>): Boolean = withLock(true) { internalAddAll(elements) }

  override fun clear() = withLock(true) {
    internalClear()
  }

  private fun internalClear() {
    while (sizeCount.value != 0) {
      internalRemoveAt(0)
    }
    head.value = null
    tail.value = null
    sizeCount.value = 0
    nodePool.clear()
  }

  override fun contains(element: T): Boolean = withLock(false) { internalFindFirst(element) != null }

  override fun containsAll(elements: Collection<T>): Boolean =
    withLock(false) { elements.all { internalFindFirst(it) != null } }

  override fun get(index: Int) = withLock(false) { internalNodeAt(index).nodeValue }

  override fun indexOf(element: T): Int = withLock(false) { internalFindFirstIndex(element).index }

  override fun isEmpty() = withLock(false) { sizeCount.value == 0 }

  override fun remove(value: T): Boolean = withLock(true) { internalRemove(value) }

  override fun removeAll(elements: Collection<T>): Boolean = withLock(true) { elements.all { internalRemove(it) } }

  override fun removeAt(index: Int): T = withLock(true) {
    internalRemoveAt(index)
  }

  override fun set(index: Int, element: T): T = withLock(true) {
    val node = internalNodeAt(index)
    val old = node.nodeValue
    node.internalSet(element)

    return old
  }

  class Node<T>(val list: AbstractSharedLinkedList<T>) {

    private val atomicValue: AtomicReference<T?> = AtomicReference(null)
    val nodeValue: T
      get() = atomicValue.value!!
    val prev = AtomicReference<Node<T>?>(null)
    val next = AtomicReference<Node<T>?>(null)
    private val removed = AtomicBoolean(false)

    internal fun recycle() {
      prev.value = null
      next.value = null
      removed.value = false
    }

    internal fun clearValue() {
      atomicValue.value = null
    }

    fun set(t: T) = list.withLock(true) {
      internalSet(t)
    }

    /**
     * Set value
     *
     * Mutates list
     */
    internal fun internalSet(t: T) {
      checkNotRemoved()
      atomicValue.value = t.freeze()
    }

    /**
     * For iterators, make sure 'next' is always updated last so we don't need to lock nav.
     */
    fun add(t: T): Boolean = list.withLock(true) {
      internalAdd(t)
    }

    /**
     * Adds new value before me.
     *
     * Mutates list
     */
    internal fun internalAdd(t: T): Boolean {
      checkNotRemoved()
      val ins = list.makeNode(t)

      ins.freeze()

      val prevNode = prev.value
      ins.prev.value = prevNode
      ins.next.value = this

      list.sizeCount.incrementAndGet()

      //If replacing 0
      if (prevNode == null) {
        list.head.value = ins
      } else {
        prevNode.next.value = ins
      }

      prev.value = ins

      return true
    }

    internal fun checkNotRemoved() {
      if (removed.value)
        throw IllegalStateException("Node is removed $this")
    }

    /**
     * Add same node to end of list.
     */
    fun readd() = list.withLock(true) {
      internalReadd()
    }

    /**
     * Re-add myself back to list.
     *
     * Mutates list
     */
    internal fun internalReadd() {
      checkNotRemoved()
      internalRemove(false)
      prev.value = null
      next.value = null
      list.internalAdd(this)
    }

    /**
     * For iterators, make sure 'next' is always updated last so we don't need to lock nav.
     */
    fun remove(permanent: Boolean = true) = list.withLock(true) {
      internalRemove(permanent)
    }

    val isRemoved: Boolean
      get() = removed.value

    /**
     * Removes self from list.
     *
     * Mutates list
     */
    internal fun internalRemove(permanent: Boolean = true) {
      checkNotRemoved()

      if (permanent) {
        removed.value = true
        clearValue()
        list.nodePool.push(this)
      }

      val prevNode = prev.value
      val nextNode = next.value

      list.sizeCount.decrementAndGet()

      if (nextNode == null) {
        list.tail.value = prevNode
      } else {
        nextNode.prev.value = prevNode
      }

      if (prevNode == null) {
        list.head.value = nextNode
      } else {
        prevNode.next.value = nextNode
      }
    }
  }

  private var lock: Lock = Lock()
  internal val sizeCount = AtomicInt(0)
  internal val head = AtomicReference<Node<T>?>(null)
  internal val tail = AtomicReference<Node<T>?>(null)
  internal val nodePool = ObjectPool(objectPoolSize, { Node(this) })

  internal fun makeNode(t: T): Node<T> {
    val node = nodePool.pop()
    node.recycle()
    node.internalSet(t)
    return node
  }

  internal fun internalAddAll(elements: Collection<T>): Boolean {
    elements.forEach {
      internalAdd(makeNode(it))
    }

    return true
  }

  internal fun internalNodeAt(i: Int): Node<T> {
    if (i >= sizeCount.value)
      throw IllegalArgumentException("index $i ge ${sizeCount.value}")

    var node = head.value
    var nodeCount = 0
    while (node != null) {
      if (i == nodeCount++) {
        return node
      }
      node = node.next.value
    }

    throw IllegalStateException("Bad math")
  }

  internal fun internalRemoveAt(i: Int): T {
    val node = internalNodeAt(i)
    val nodeValue = node.nodeValue
    node.internalRemove()
    return nodeValue
  }

  /**
   * Add node to list.
   *
   * Mutates list
   *
   * @param node
   * to add to list.
   */
  internal fun internalAdd(node: Node<T>): Boolean {
    node.checkNotRemoved()
    node.freeze()
    if (sizeCount.value == 0) {
      head.value = node
      tail.value = node
    } else {
      val prev = tail.value
      prev!!.next.value = node
      node.prev.value = prev
      tail.value = node
    }
    sizeCount.incrementAndGet()
    return true
  }

  internal fun internalFindFirstIndex(value: T): NodeResult<T> {
    var node = head.value
    var nodeCount = 0
    while (node != null) {
      if (node.nodeValue == value) {
        return NodeResult(nodeCount, node)
      }
      node = node.next.value
      nodeCount++
    }

    return NodeResult(-1, null)
  }

  internal fun internalFindFirst(value: T): Node<T>? = internalFindFirstIndex(value).node

  internal fun internalRemove(value: T): Boolean {
    val node = internalFindFirst(value)
    return if (node == null) {
      false
    } else {
      node.internalRemove()
      true
    }
  }

  data class NodeResult<T>(val index: Int, val node: Node<T>?)

  fun debugPrint(): String {
    val sb = StringBuilder()
    var node = head.value

    while (node != null) {
      sb.append("val: ${node.nodeValue}, pref: ${node.prev.value}, next: ${node.next.value}\n")
      node = node.next.value
    }

    return sb.toString()
  }

  internal fun lock() {
    lock.lock()
  }

  internal fun unlock() {
    lock.unlock()
  }

  internal abstract fun updated()

  internal inline fun <T> withLock(updated: Boolean, proc: () -> T): T {
    lock()
    try {
      return proc.invoke()
    } finally {
      if (updated)
        updated()
      unlock()
    }
  }
}