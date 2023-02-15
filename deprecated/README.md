# Stately - Deprecated

The Kotlin/Native runtime previously had a unique ["strict" memory model](https://www.youtube.com/watch?v=oxQ6e1VeH4M), and Stately originally existed to facilitate developing common code with those constructs, as well as some other structures for living in that world. As of Kotlin 1.7.20, that strict memory model is deprecated, and as such, so are the Stately modules built to support it.

They are sill available and will be published with Stately updates, but they are not being actively developed and, generally speaking, not actively supported (although critical fixes will probably happen).

## stately-common

Kotlin/Native state adds some concepts that don't exist in either the JVM or JS, as well as the annotation @Throws.

 `stately-common` is very simple. It includes common definitions for `freeze()`, `isFrozen`, and `ensureNeverFrozen()`, and as mentioned `@Throws`. These definitions, 2 functions, a val, and an annotation, are often source-copied into other libraries. `stately-common`'s sole purpose is to define these as minimally as possible, to be included in apps or other libraries.

On native, these values delegate or are typealiased to their platform definitions. In JS and the JVM, they do nothing.

### Config

```groovy
commonMain {
    dependencies {
        implementation 'co.touchlab:stately-common:1.2.0'
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
        implementation 'co.touchlab:stately-isolate:1.2.0'
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
        implementation 'co.touchlab:stately-iso-collections:1.2.0'
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

## We're Hiring!

Touchlab is looking for a Mobile Developer, with Android/Kotlin experience, who is eager to dive into Kotlin Multiplatform Mobile (KMM) development. Come join the remote-first team putting KMM in production. [More info here](https://go.touchlab.co/careers-gh).

## Primary Maintainer

[Kevin Galligan](https://github.com/kpgalligan/)

![Image of Kevin](https://avatars.githubusercontent.com/u/68384?s=140&v=4)

*Ping me on twitter [@kpgalligan](https://twitter.com/kpgalligan/) if you don't get a timely reply!* -Kevin

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
