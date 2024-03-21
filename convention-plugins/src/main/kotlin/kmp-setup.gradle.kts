import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension

plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()
    js {
        nodejs()
        browser()
    }
    @OptIn(org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
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
}

rootProject.the<NodeJsRootExtension>().apply {
    nodeVersion = "21.0.0-v8-canary202309143a48826a08"
    nodeDownloadBaseUrl = "https://nodejs.org/download/v8-canary"
}