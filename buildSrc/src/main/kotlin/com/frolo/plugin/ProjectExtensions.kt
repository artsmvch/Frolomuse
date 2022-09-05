package com.frolo.plugin

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.gradle.AppExtension
import org.gradle.api.Project

internal val Project.androidAppExtension: AppExtension get() {
    val extension = this.extensions.findByName("android")
        ?: throw NullPointerException("Project does not have an android extension")
    if (extension !is AppExtension) {
        throw NullPointerException("Unknown android extension")
    }
    return extension
}

internal fun Project.configureAndroidApp(block: (AppExtension) -> Unit) {
    androidAppExtension.apply(block)
}