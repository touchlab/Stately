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
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.testHelp)
        }
    }
}