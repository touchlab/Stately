plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.buildconfig) apply false
    alias(libs.plugins.binaryCompatibilityValidator)
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.touchlab.docusaurusosstemplate)
    alias(libs.plugins.mavenPublish) apply false
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