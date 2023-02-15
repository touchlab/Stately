plugins {
    kotlin("multiplatform")
}

val GROUP: String by project
val VERSION_NAME: String by project
val TESTHELP_VERSION: String by project

group = GROUP
version = VERSION_NAME

kotlin {
    jvm()
    js {
        nodejs()
        browser()
    }
    macosX64()
    iosArm32()
    iosArm64()
    iosX64()
    watchosArm32()
    watchosArm64()
    watchosX86()
    watchosX64()
    tvosArm64()
    tvosX64()

    macosArm64()
    iosSimulatorArm64()
    watchosSimulatorArm64()
    tvosSimulatorArm64()

    mingwX64()
    mingwX86()
    linuxX64()
    linuxArm32Hfp()
    linuxMips32()

    androidNativeArm32()
    androidNativeArm64()
    androidNativeX86()
    androidNativeX64()

    val commonMain by sourceSets.getting {
        dependencies {
            implementation(kotlin("stdlib-common"))
            implementation(project(":stately-concurrency"))
        }
    }
    val commonTest by sourceSets.getting {
        dependencies {
            implementation(kotlin("test-common"))
            implementation(kotlin("test-annotations-common"))
            implementation("co.touchlab:testhelp:$TESTHELP_VERSION")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0")
        }
    }

    val jvmMain by sourceSets.getting {
        dependsOn(commonMain)
        dependencies {
            implementation(kotlin("stdlib-jdk8"))
        }
    }
    val jvmTest by sourceSets.getting {
        dependsOn(commonTest)
        dependencies {
            implementation(kotlin("test"))
            implementation(kotlin("test-junit"))
        }
    }

    val jsMain by sourceSets.getting {
        dependsOn(commonMain)
        dependencies {
            implementation(kotlin("stdlib-js"))
        }
    }
    val jsTest by sourceSets.getting {
        dependsOn(commonTest)
        dependencies {
            implementation(kotlin("test-js"))
        }
    }

    val nativeMain by sourceSets.creating
    nativeMain.dependsOn(commonMain)
    val nativeTest by sourceSets.creating
    nativeTest.dependsOn(commonTest)

    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().all {
        val mainSourceSet = compilations.getByName("main").defaultSourceSet
        val testSourceSet = compilations.getByName("test").defaultSourceSet
        mainSourceSet.dependsOn(nativeMain)
        testSourceSet.dependsOn(nativeTest)
    }
}

apply(from = "../gradle/gradle-mvn-mpp-push.gradle")