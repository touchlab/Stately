
include(":stately-strict")
include(":stately-concurrency")
include(":stately-concurrent-collections")

// Deprecated modules
include(":stately-common")
include(":stately-collections")
include(":stately-isolate")
include(":stately-iso-collections")

project(":stately-common").projectDir = File("deprecated/stately-common")
project(":stately-collections").projectDir = File("deprecated/stately-collections")
project(":stately-isolate").projectDir = File("deprecated/stately-isolate")
project(":stately-iso-collections").projectDir = File("deprecated/stately-iso-collections")