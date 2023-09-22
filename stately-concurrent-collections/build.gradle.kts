@file:Suppress("PropertyName")

plugins {
    kotlin("multiplatform")
    id("com.vanniktech.maven.publish")
}

val GROUP: String by project
val VERSION_NAME: String by project

group = GROUP
version = VERSION_NAME

kotlin {
    @Suppress("OPT_IN_USAGE")
    targetHierarchy.default()
    jvm()
    js {
        nodejs()
        browser()
    }
    @Suppress("OPT_IN_USAGE")
    wasm {
        browser()
        binaries.executable()
    }
    macosX64()
    iosArm64()
    iosX64()
    watchosArm32()
    watchosArm64()
    watchosX64()
    watchosDeviceArm64()
    tvosArm64()
    tvosX64()

    macosArm64()
    iosSimulatorArm64()
    watchosSimulatorArm64()
    tvosSimulatorArm64()
    watchosDeviceArm64()

    mingwX64()
    linuxX64()
    linuxArm64()

    androidNativeArm32()
    androidNativeArm64()
    androidNativeX86()
    androidNativeX64()
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":stately-concurrency"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.testHelp)
                implementation(libs.coroutines.test)
            }
        }

        val jsWasmMain by creating {
            dependsOn(commonMain)
            getByName("jsMain").dependsOn(this)
            getByName("wasmMain").dependsOn(this)
        }

        val jsWasmTest by creating {
            dependsOn(commonTest)
            getByName("jsTest").dependsOn(this)
            getByName("wasmTest").dependsOn(this)
        }

        val nativeMain by getting
        val nativeTest by getting
    }
}
