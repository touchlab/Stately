package org.jetbrains.kotlin

class DistroFunctions{
    static String findDistroPath(rootProject) {
        String konanPath = null

        if (!rootProject.hasProperty('konanDevPath')) {
            File buildDir = new File(rootProject.getProjectDir(), "build")
            File distFolder = new File(buildDir, "kndist")

            if (!distFolder.exists()) {
                File zipFile = new File(buildDir, "kndist.zip")
                File unzipFolder = new File(buildDir, "distextract")

//rootProject.property("kotlinNativeArchive")
                DistroHelperKt.downloadKotlinNativeDistro("https://github.com/JetBrains/kotlin-native/archive/v1.3.0.zip", zipFile)
                DistroHelperKt.unzipFileToFolder(zipFile, unzipFolder)
                DistroHelperKt.distroPath(unzipFolder).renameTo(distFolder)
            }

            konanPath = distFolder.path
        } else {
            konanPath = rootProject.property("konanDevPath")
        }

        return konanPath
    }
}
