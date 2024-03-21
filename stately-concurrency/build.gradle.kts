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
    sourceSets {
        commonMain.dependencies {
            implementation(project(":stately-strict"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.testHelp)
        }
    }
}
