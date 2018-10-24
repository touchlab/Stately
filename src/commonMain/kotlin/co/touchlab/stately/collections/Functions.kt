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

/**
 * Creates a copy-on-write list. On the JVM, will return
 * [https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/CopyOnWriteArrayList.html](CopyOnWriteArrayList).
 * On Native, there is a native implementation which maintains and updates a frozen ArrayList.
 */
expect fun <T> frozenCopyOnWriteList(collection:Collection<T>? = null):MutableList<T>

fun <T> frozenLinkedList(stableIterator:Boolean = false):MutableList<T> = if(stableIterator){
    CopyOnIterateLinkedList()
}else{
    SharedLinkedList()
}

fun <K, V> frozenHashMap(initialCapacity:Int = 16, loadFactor:Float = 0.75.toFloat()):MutableMap<K, V> =
        SharedHashMap(initialCapacity, loadFactor)

fun <K, V> frozenLruCache(maxCacheSize:Int, onRemove:(MutableMap.MutableEntry<K, V>) -> Unit = {}):LruCache<K, V> =
        SharedLruCache(maxCacheSize, onRemove)

/**
 * Creates a list from an Iterator.
 */
fun <T> Iterator<T>.toList():List<T>{
    val result = mutableListOf<T>()
    while (hasNext()) {result.add(next())}
    return result
}