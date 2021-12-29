# Stately

Stately is a state utility library to facilitate state management in Kotlin Multiplatform.

Stately originally provided support in common code for some of the features specific to the Kotlin/Native strict
memory model. That memory model is being replaced with a new memory model that shares memory across threads unrestricted,
more like the JVM and Objc/Swift (and others like C/C++, etc).

## Status as of Kotlin 1.6.10

We are developing and publishing a new version of Stately that will only support the newer memory model. 
Several Stately modules that primarily made sense in the original Kotlin/Native world have been removed. These include
stately-collections (mutable collections implemented with atomics), and stately-isolate/stately-iso-collections (thread-confined
mutable state). `stately-common` has had most of it's code removed and is now `stately-strict`.

The changes are in flux and will likely go through multiple rounds. The library itself may eventually go away as more 
official libraries emerge, but we'll see how that evolves over 2022/23.

# Modules

## stately-concurrency

This module contains atomics and locks, which will be needed to implement concurrent code in common Kotlin. Much of this
also exists in [kotlinx.atomicfu](https://github.com/Kotlin/kotlinx.atomicfu), but we find setup of that library to be 
relatively complex, so we'll continue to publish Stately's version. That may change if AtomicFu becomes a little more
"user friendly". However, this module is relatively lightweight, so we may simply continue to publish it.

## stately-concurrent-collections

This is a relatively simple implementation of concurrent-safe Mutable collections. It includes a Set, Map, and a List 
implementation. All of these delegate internally to Kotlin's std MutableSet, MutableMap, and MutableList respectively.

```kotlin
val l = ConcurrentMutableList<SomeData>()
l.add(SomeData("a"))
async(Dispatchers.Default) {
 //Different Thread!
 l.add(SomeData("b"))
}
```

## stately-strict

Stately common previously had common definitions that helped code to the Kotlin/Native strict memory model. When moving
to the new memory model, it is likely you'll need to interact with libraries that were written with the old strict memory
model in mind, and there may be times where you need to debug these. `stately-strict` will get a handful of definitions
that may be helpful to do that.

Currently the only thing defined in the module is `Any.ensureNeverFrozen()`. A library may freeze some of your code
and you can use this to help figure out where that is happening. However, you can also disable freezing altogether,
so we'll have to see if this kind of support has any value going forward.

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
