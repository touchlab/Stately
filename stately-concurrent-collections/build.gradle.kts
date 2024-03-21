@file:Suppress("PropertyName")

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    kotlin("multiplatform")
    id("com.vanniktech.maven.publish")
    id("kmp-setup")
}

val GROUP: String by project
val VERSION_NAME: String by project

group = GROUP
version = VERSION_NAME

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":stately-concurrency"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.testHelp)
            implementation(libs.coroutines.test)
        }
    }
}
