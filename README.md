# Stately

Stately is a state utility library to facilitate state management in Kotlin Multiplatform.

Kotlin JVM has the same rules around concurrency and state that Java has. In essence, multiple threads can access shared state in an unrestricted fashion, and it is up to the developer to ensure safe concurrency. Kotlin/Native, on the other hand, introduces new restrictions around concurrent state access ([more](https://dev.to/touchlab/practical-kotlin-native-concurrency-ac7) [info](https://www.youtube.com/watch?v=oxQ6e1VeH4M)). Additionally, Kotlin/JS lives in the Javascript threading world, which means just the one thread.

Stately provides various modules to facilitate writing shared code within these different worlds.

## stately-common

Kotlin/Native state adds some concepts that don't exist in either the JVM or JS, as well as the annotation @Throws.

 `stately-common` is very simple. It includes common definitions for `freeze()`, `isFrozen`, and `ensureNeverFrozen()`, and as mentioned `@Throws`. These definitions, 2 functions, a val, and an annotation, are often source-copied into other libraries. `stately-common`'s sole purpose is to define these as minimally as possible, to be included in apps or other libraries.

On native, these values delegate or are typealiased to their platform definitions. In JS and the JVM, they do nothing.

### Config

```groovy
commonMain {
    dependencies {
        implementation 'co.touchlab:stately-common:1.0.x'
    }
}
```

## stately-concurrency

`stately-concurrency` includes some concurrency support classes. These include a set of `Atomicxxx` classes, a `Lock`, a `ThreadLocal` container, and a class `ThreadRef` that allows you to hold a thread id.

Much of the functionality of this module is similar to [atomic-fu](https://github.com/Kotlin/kotlinx.atomicfu). They differ in some ways, so while they both cover much of the same ground, Stately's version still has some use.

`ThreadRef` is unique to Stately. It allows you to capture a reference to the id of the thread in which it was created, and ask if the current thread is the same. Just FYI, it does *not* keep a reference to the actual thread. Just an id. Usage looks like this:

```kotlin
fun useRef(){
  val threadRef = ThreadRef()
  threadRef.same() // <- true
  backgrundThread {
    threadRef.same() // <- false
  }
}
```

### Config

```groovy
commonMain {
    dependencies {
        implementation 'co.touchlab:stately-concurrency:1.0.x'
    }
}
```

## stately-isolate

`stately-isolate` creates mutable state in a special state thread, and restricts access to that state from the same thread. This allows the state held by an instance of `IsolateState` to remain mutable. State coming in and going out must be frozen, but the guarded state can change.

> Read more about the design in [this blog post](https://dev.to/touchlab/kotlin-native-isolated-state-50l1).

The obvious use case is for collections. Example usage:

```kotlin
fun usage(){
    val cacheMap = IsolateState { mutableMapOf<String, String>() }
    val key = "abc"
    val value = "123"
    cacheMap.access { it.put(key, value) }
    val valueString = cacheMap.access { it.get(key) }
    println(valueString) // <- will print '123'
}
```

The `cacheMap` above can be called from multiple threads. The lambda passed to the `access` method will be invoked on the same thread that the state was created on. Because it is a single thread, access is serialized and thread-safe.

You can create other instances of `IsolateState` by forking the parent instance.

```kotlin
fun usage(){
    val cacheMap = IsolateState { mutableMapOf<String, String>() }
    val key = "abc"
    val value = "123"
    cacheMap.access { it.put(key, value) }
    
    //Fork state
    val entrySet = cacheMap.access { map -> 
        IsolateState(cacheMap.fork(map.entries)) 
    }
    
    val valueString = entrySet.access { entries -> entries.first().value }
    println(valueString) // <- will print '123'
}
```

You can create a class that extends `IsolateState`  and provides for simpler access.

```kotlin
class SimpleMap<K, V>: IsolateState<MutableMap<K, V>>({ mutableMapOf()})
{
    fun put(key:K, value:V):V? = access { it.put(key, value) }
    fun get(key: K):V? = access { it.get(key) }
}
```

`stately-iso-collections` implements collections by extending `IsolateState` in this manner.

You must dispose of `IsolateState` instances to avoid memory leaks.

```kotlin
fun usage(){
    val cacheMap = IsolateState { mutableMapOf<String, String>() }
    cacheMap.dispose()
}
```

### Config

```kotlin
commonMain {
    dependencies {
        implementation 'co.touchlab:stately-isolate:1.0.x'
    }
}
```

## stately-iso-collections

This is a set of mutable collections implemented with `IsolateState`. The set currently includes a `MutableSet`, `MutableList`, 
 `MutableMap`, and an implementation of `ArrayDeque` that is being added to the Kotlin stdlib in 1.3.70.



### Config

```kotlin
commonMain {
    dependencies {
        implementation 'co.touchlab:stately-iso-collections:1.0.x'
    }
}
```

## stately-collections

A set of collections that can be shared and accessed between threads. This is pretty much deprecated, but we have no plans to remove it as some production apps use it.

However, ***we would strongly suggest you use `stately-isolate` and `stately-iso-collections` instead.*** Collections implemented with `stately-isolate` are far more flexible and absolutely CRUSH the original `stately-collections` in performance benchmarks. See [blog post](https://dev.to/touchlab/kotlin-native-isolated-state-50l1).

## Usage

Dependencies can be specified in the common source set, as shown above, if you have Gradle metadata enabled in `settings.gradle`.

```groovy
enableFeaturePreview('GRADLE_METADATA')
```

License
=======

    Copyright 2020 Touchlab, Inc.
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
       http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
