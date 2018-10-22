# Stately Sample

The stately sample is a pair of android an iOS apps, as well as shared code.
The apps both have one text box that takes a string, a button to add that value to the shared collections, and another
button that will print the current state to standard out.

*NOTE:* This sample is built and run in Intellij EAP. It has not been tests with Android Studio due to gradle version
restrictions. This situation should resolve in the coming weeks.

## Building

On the command line, run './gradlew build'. This will build the code as well as prep the iOS side.

To open Xcode, in terminal

```
cd iosApp
open iosApp.xcodeproj 
```

Select a simulator and run.

For Android, create an Android run config and run.

## Points of Interest

### Setup

The dependency for Stately is added in the [shared build.gradle](app/build.gradle) file


```
sourceSets {
    commonMain {
        dependencies {
            implementation 'org.jetbrains.kotlin:kotlin-stdlib-common'
            implementation 'co.touchlab.stately:Stately:0.3.1-kn1.0rc-a1'
        }
    }
    //yada yada
```

The sample itself is created from Intellij's mobile multiplatform app sample. The only major change you 
should be aware of is adding the gradle metadata switch to [settings.gradle](settings.gradle).

```
enableFeaturePreview('GRADLE_METADATA')
```

That is required to resolve dependencies.

### Code

The sample code is in [sample/State.kt](app/src/commonMain/kotlin/sample/State.kt). This is a top level
object, which means with regards to Kotlin/Native, it will be immutable once created.

A set of collections is created for this object, as well as some threaded workers.

```kotlin
val cowList = frozenCopyOnWriteList<SampleData>()
val sharedList = frozenLinkedList<SampleData>()
val coiSharedList = frozenLinkedList<SampleData>(stableIterator = true)
val sharedMap = frozenHashMap<String, SampleData>()

val lruRemovedCount = AtomicInt(0)
val lruCache = frozenLruCache<String, SampleData>(5) {
    lruRemovedCount.increment()
}

val workers = Array(4){createWorker()}

```

These are created frozen, and are shared between threads.

When you call 'putSample', the string is inserted into the various collections, from multiple threads.
Calling 'printAll' shows you the output.