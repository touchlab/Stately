import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    kotlin("multiplatform")
    id("com.vanniktech.maven.publish")
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
    jvm()
    js {
        nodejs()
        browser()
    }
    @Suppress("OPT_IN_USAGE")
    wasmJs {
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

    @Suppress("OPT_IN_USAGE")
    applyDefaultHierarchyTemplate {
        common {
            group("jsAndWasmJs") {
                withJs()
                withWasm()
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":stately-strict"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.testHelp)
        }

        // val nativeCommonMain by creating {
        //     dependsOn(commonMain)
        // }
        // val nativeCommonTest by creating {
        //     dependsOn(commonTest)
        // }
        //
        // val darwinMain by creating {
        //     dependsOn(nativeCommonMain)
        // }
        //
        // val pthreadMain by creating {
        //     dependsOn(nativeCommonMain)
        // }
        //
        // val mingwMain by getting {
        //     dependsOn(nativeCommonMain)
        // }
        //
        // val pthreadAndroidMain by creating {
        //     dependsOn(nativeCommonMain)
        // }
        //
        // targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().all {
        //     val mainSourceSet = compilations.getByName("main").defaultSourceSet
        //     val testSourceSet = compilations.getByName("test").defaultSourceSet
        //
        //     mainSourceSet.dependsOn(
        //         when {
        //             konanTarget.family.isAppleFamily -> darwinMain
        //             konanTarget.family == org.jetbrains.kotlin.konan.target.Family.LINUX -> pthreadMain
        //             konanTarget.family == org.jetbrains.kotlin.konan.target.Family.MINGW -> mingwMain
        //             konanTarget.family == org.jetbrains.kotlin.konan.target.Family.ANDROID -> pthreadAndroidMain
        //             else -> nativeCommonMain
        //         }
        //     )
        //
        //     testSourceSet.dependsOn(nativeCommonTest)
        // }
    }
}
