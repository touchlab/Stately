plugins {
    kotlin("multiplatform") apply false
    id("com.android.library") version "7.4.2" apply false
    id("com.github.gmazzo.buildconfig") version "2.1.0" apply false
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.13.2"
    id("org.jetbrains.dokka") version "1.8.20" apply false
    id("co.touchlab.touchlabtools.docusaurusosstemplate") version "0.1.10"
    id("com.vanniktech.maven.publish") version "0.25.3" apply false
}
apiValidation {
    nonPublicMarkers.add("co.touchlab.kermit.ExperimentalKermitApi")
    ignoredProjects.addAll(
        listOf(
            "stately-collections",
            "stately-common",
            "stately-iso-collections",
            "stately-isolate"
        )
    )
}

val GROUP: String by project
val VERSION_NAME: String by project

allprojects {
    group = GROUP
    version = VERSION_NAME
}

allprojects {
    repositories {
        mavenCentral()
        google()
    }
}