# Stately

Stately is a state utility library to facilitate state management in Kotlin Multiplatform. It was originally written to facilitate development with the strick Kotlin/Native memory model. As of Kotlin 1.7.20, the strict model is deprecated, and the [releveant modules of Stately have also been deprecated](deprecated) but are still published and available.

Stately currently provides concurrencly primitives and concurrent collections. 

## stately-concurrency

`stately-concurrency` includes some concurrency support classes. These include a set of `Atomicxxx` classes, a `Lock`, a `ThreadLocal` container, a `Synchronizable` type, and a class `ThreadRef` that allows you to hold a thread id.

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

The `Synchronizable` type allows us to use the JVM's `synchronized` but in common, and with Kotlin/Native which doesn't natively support it. 

```kotlin
class MyMutableData(private var count: Int = 0) : Synchronizable() {
    fun add() {
        synchronize { count++ }
    }

    val myCount: Int
        get() = synchronize { count }
}
```

Your type should extend `Synchronizable`, which on the JVM is typealiased to `Any`. Then you can use `synchronize` as in the example above. 

### Config

```groovy
commonMain {
    dependencies {
        implementation("co.touchlab:stately-concurrency:2.0.0-rc1")
    }
}
```

## stately-concurrent-collections

A set of relatively simple mutable collections that are thread safe.

```kotlin
val list = ConcurrentMutableList<Int>()
val list.add(42)
```

### Config

```groovy
commonMain {
    dependencies {
        implementation("co.touchlab:stately-concurrent-collections:2.0.0-rc1")
    }
}
```

> ## Subscribe!
>
> We build solutions that get teams started smoothly with Kotlin Multiplatform Mobile and ensure their success in production. Join our community to learn how your peers are adopting KMM.
 [Sign up here](https://go.touchlab.co/newsletter-gh)!

## Primary Maintainer

[Kevin Galligan](https://github.com/kpgalligan/)

![Image of Kevin](https://avatars.githubusercontent.com/u/68384?s=140&v=4)

*Ping me on twitter [@kpgalligan](https://twitter.com/kpgalligan/) if you don't get a timely reply!* -Kevin

License
=======

    Copyright 2022 Touchlab, Inc.
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
       http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
