# Stately

Stately is a state utility library to facilitate state management in Kotlin Multiplatform.

The core library consists primarily of some useful expect/actual definitions, Atomics, and Locks. There is also a library
with a set of multithreaded collection classes that will allow multithreaded mutation in Kotlin/Native.

Kotlin/Native has a fairly different model of concurrency than what JVM developers are used to. This includes very 
different rules around sharing state. These rules are intended to reduce the likelihood of concurrency related issues.
[Read this](https://medium.com/@kpgalligan/kotlin-native-stranger-threads-ep-2-208523d63c8f) for more info.

> ## **We're Hiring!**
>
> Touchlab is looking for Android-focused mobile engineers, experienced with Kotlin and 
> looking to get involved with Kotlin Multiplatorm in the near future. [More info here](https://on.touchlab.co/30sMEjR).

## Functions/Extensions

### freeze()

Multiplatform definition for Kotlin/Native 'freeze'.

### isFrozen()

Call on any object to find out if it's frozen.

### isNativeFrozen()

Will return true on non-native platforms. On native, will return true if frozen. Usually you're trying to figure out if
an instance can be shared among threads. Because the rules are different, the answer is always "true" on JVM and JS. This
method makes that logic simpler.

### isNative (val)

Simple way to find out if we're in a native context.

### Iterator.toList()

Converts an Iterator to a List. This is an extension function that works on any Iterator. Mostly useful for testing.

## Atomics

Atomic value definitions for Int, Long, Boolean, and object references.

## Lock

Mutex lock for various platforms. It is important to note that there is an extension method called `close()`. This only
needs to be called if you are on a non Apple native platform (Windows & Linux).
You can safely ignore `close` on JVM, JS, iOS, and MacOS.

## Collections

Collections allow mutable collections in frozen code. They've been moved to a separate package as the current form will 
likely be removed in the future. Either a faster version will be created, or "relaxed mode" for Kotlin/Native will make
them relatively useless. However, they do work and are available.

## Important note for all collections!!!

AtomicReference can leak memory in Kotlin Native. To avoid leaking memory, you should call `clear()`
on any collection when done using it.

### Copy On Write List

A MutableList that updates a stable copy on each edit. When you get an iterator, that iterator remains stable regardless
of operations happening on the list. The JVM version is actually [https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/CopyOnWriteArrayList.html](CopyOnWriteArrayList.java). 
The Native version updates a frozen ArrayList internally by copying and replacing on edits.

```kotlin
val list:MutableList<SampleData> = frozenCopyOnWriteList<SampleData>()
```

### Shared Linked List

Rather than copying the full list on each edit, this structure uses atomic references in the nodes to allow modification
that is reasonably performant relative to the Copy On Write list in situations that may have larger lists and/or frequent updates.

It's important to note, performance is OK *relative* to copying the list on each edit. The list locks aggressively to 
achieve consistency. There are lockless algorithms for linked lists that may be explored in the future.

There are two versions.

### SharedLinkedList

Iterators reflect changes to the underlying list. If values are added/removed while iterating, your iterator will
see them.

```kotlin
val list:MutableList<SampleData> = frozenLinkedList<SampleData>()

```

### CopyOnIterateLinkedList

Iterators are stable. Calling iterator creates an internal copy of the list which the iterator uses. Underlying changes
to the source list are NOT reflected in the iterator. This will trigger a lock and copy on calling iterator, which 
may be a performance consideration with large lists.

Similar to CopyOnWriteList, and likely to perform better in general cases. It is a linked list, however, so indexed 
access requires a scan.

```kotlin
val list:MutableList<SampleData> = frozenLinkedList<SampleData>(stableIterator = true)

```

### Hash Map

A standard hash map architected as a simple Java-like implementation. Optionally pass in initial capacity and load factor.
Will grow as needed. This also locks on most operations, so from a performance perspective I would expect it 
to fall pretty far behind the standard single threaded implementation, but from an order of operations perspective, it should be similar.

One note. In Java 8 (not sure about kotlin native), in buckets, after length 8, a tree is used instead of a linked list.
Worst case performance then flattens out to lgN (ish). Java 7, and our implementation, do not, so expect worst case to
be N. Try to use good hashes.

```kotlin
val sharedMap:MutableMap<String, SampleData> = frozenHashMap<String, SampleData>()
```

### LRU Cache

Hash map and linked list combined into an LRU cache implementation.

You can supply a callback to be notified when an entry is removed.

```kotlin
val lruRemovedCount = AtomicInt(0)
val lruCache = frozenLruCache<String, SampleData>(5) {
    lruRemovedCount.increment()
}
```

Note that the lambda for onRemove is optional. However, since it is multithreaded, be aware that anything in there will
be frozen.

## Usage

Stately is a Kotlin multiplatform library, and uses the standard multiplatform Gradle plugin.
Adding dependencies to gradle looks like this. It can go in the common module, or
the platform specific modules if desired.

```groovy
dependencies {
    implementation "co.touchlab:stately:0.9.x"
    implementation "co.touchlab:stately-collections:0.9.x"
}
```

Make sure your Gradle vesion is 5.3+, and that you have metadata enabled in `settings.gradle`

```groovy
enableFeaturePreview('GRADLE_METADATA')
```

## Sample App

The sample app is updated!!! It doesn't do much, but demos 
several of the structures.

License
=======

    Copyright 2018 Touchlab, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
