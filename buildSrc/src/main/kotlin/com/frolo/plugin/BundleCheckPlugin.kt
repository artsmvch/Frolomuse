package com.frolo.plugin

import com.android.build.gradle.api.ApplicationVariant
import com.frolo.utils.FileUtils
import org.gradle.api.Plugin
import org.gradle.api.Project

class BundleCheckPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.configureAndroidApp { ext ->
            ext.applicationVariants.all {
                checkBundleSize(target, this)
            }
        }
    }

    private fun checkBundleSize(project: Project, variant: ApplicationVariant) {
        val bundleTaskProvider = project.tasks.named("bundle${variant.name.capitalize()}")
        bundleTaskProvider.configure {
            doLast {
                variant.outputs.all {
                    val bundleFile = FileUtils.getBundleOutputFile(project, variant)
                    if (bundleFile == null || !bundleFile.exists()) {
                        throw NullPointerException("AAB output file for ${variant.name} " +
                                "variant not found")
                    }
                    val fileSizeInBytes = bundleFile.length()
                    //val fileSizeInMegabytes = fileSizeInBytes / (1024 * 1024)
                    if (fileSizeInBytes > MAX_AAB_FILE_SIZE)  {
                        val diff = fileSizeInBytes - MAX_AAB_FILE_SIZE
                        throw IllegalStateException("AAB output file is too large. " +
                                "The limit is $MAX_AAB_FILE_SIZE bytes. The actual size is $fileSizeInBytes. " +
                                "The limit exceeded by $diff")
                    }
                    println("AAB output file size for ${variant.name} variant is $fileSizeInBytes bytes")
                }
            }
        }
    }

    companion object {
        private const val MAX_AAB_FILE_SIZE = 150 * 1024 * 1024
    }
}