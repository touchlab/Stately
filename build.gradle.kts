plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.buildconfig) apply false
    alias(libs.plugins.binaryCompatibilityValidator)
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.touchlab.docusaurusosstemplate)
    alias(libs.plugins.mavenPublish) apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.2.0" apply false
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

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.4.0")
        enableExperimentalRules.set(true)
        verbose.set(true)
        filter {
            exclude { it.file.path.contains("build/") }
        }
    }

    afterEvaluate {
        tasks.named("check") {
            dependsOn(tasks.getByName("ktlintCheck"))
        }
    }
}