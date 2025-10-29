plugins {
    kotlin("multiplatform")
    id("com.vanniktech.maven.publish")
    id("kmp-setup")
}

@Suppress("ktlint:standard:property-naming")
val GROUP: String by project

@Suppress("ktlint:standard:property-naming")
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
