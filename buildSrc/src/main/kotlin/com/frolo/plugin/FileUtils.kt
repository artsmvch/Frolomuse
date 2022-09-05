package com.frolo.plugin

import org.gradle.api.Project
import com.android.build.gradle.api.ApplicationVariant
import java.io.File

internal object FileUtils {
    fun getBundleOutputFile(project: Project, variant: ApplicationVariant): File? {
        val bundleOutputDir = File(project.buildDir, "outputs/bundle/${variant.name}")
        if (!bundleOutputDir.exists() || !bundleOutputDir.isDirectory) {
            return null
        }
        return bundleOutputDir.listFiles()
            .orEmpty()
            .filter { it.extension == "aab" }
            // Get the most recently modified one
            .maxByOrNull { it.lastModified() }
    }
}