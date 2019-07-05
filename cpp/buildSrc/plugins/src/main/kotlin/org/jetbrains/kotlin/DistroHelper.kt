package org.jetbrains.kotlin

import org.apache.commons.io.FileUtils
import org.zeroturnaround.zip.ZipUtil
import java.io.File
import java.net.URL
import java.util.*
import org.gradle.api.Project
import org.gradle.api.initialization.ProjectDescriptor
import org.gradle.initialization.DefaultProjectDescriptor

/*fun distroFinder(rootProject: DefaultProjectDescriptor):String{
    return if (!rootProject.hasProperty("konanDevPath")) {
        val buildDir = File(rootProject.projectDir, "build")
        val distFolder = File(buildDir, "kndist")

        if (!distFolder.exists()) {
            val zipFile = File(buildDir, "kndist.zip")
            val unzipFolder = File(buildDir, "distextract")

            downloadKotlinNativeDistro(rootProject.property("kotlinNativeArchive") as String, zipFile)
            unzipFileToFolder(zipFile, unzipFolder)
            distroPath(unzipFolder).renameTo(distFolder)
        }

        distFolder.path
    } else {
        rootProject.property("konanDevPath") as String
    }
}*/

fun downloadKotlinNativeDistro(path:String, dstFile:File){
    val url = URL(path)
    FileUtils.copyURLToFile(url, dstFile)
}

fun distroPath(unzipFolder: File): File{
    val folders: Array<File> = unzipFolder
            .listFiles { pathname -> pathname?.name?.contains("kotlin-native") ?: false }

    Arrays.sort(folders)

    return folders.last()
}

fun unzipFileToFolder(zipFile: File, extractFolder: File){
    ZipUtil.unpack(zipFile, extractFolder)
}


