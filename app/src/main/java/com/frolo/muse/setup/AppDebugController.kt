package com.frolo.muse.setup

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import com.frolo.muse.di.ApplicationScope
import java.io.File
import java.lang.RuntimeException
import javax.inject.Inject


@ApplicationScope
class AppDebugController @Inject constructor(
    private val context: Application
) {

    private val activityManager: ActivityManager?
        get() = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    private val mainHandler: Handler by lazy { Handler(context.mainLooper) }

    fun crash() {
        mainHandler.post {
            throw RuntimeException("Crash!")
        }
    }

    fun killCompletely() {
        finishMainTask()
        stopAllServices()
        killProcess()
    }

    fun killProcess() {
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    fun finishMainTask() {
        val manager = activityManager ?: return
        manager.appTasks.orEmpty()
            .filter { task ->
                val baseIntent = task.taskInfo.baseIntent
                baseIntent.component?.packageName == context.packageName
                    && baseIntent.categories.contains(Intent.CATEGORY_LAUNCHER)
                    && baseIntent.action?.contains(Intent.ACTION_MAIN) == true
            }
            .forEach { task ->
                task.finishAndRemoveTask()
            }
    }

    fun stopAllServices() {
        val manager = activityManager ?: return
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (service.service.packageName != context.packageName) {
                continue
            }
            val intent = Intent().apply {
                component = service.service
            }
            context.stopService(intent)
        }
    }

    fun clearUserData() {
        activityManager?.clearApplicationUserData()
    }

    fun clearAllPreferences() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val sharedPreferenceDir = File(context.dataDir, SHARED_PREFS_FOLDER)
            clearFolder(sharedPreferenceDir)
        }
    }

    fun clearAllFiles() {
        context.filesDir?.also(::clearFolder)
    }

    fun clearCache() {
        context.cacheDir?.also(::clearFolder)
    }

    private fun clearFolder(file: File) {
        if (!file.exists() || !file.isDirectory) {
            return
        }
        file.listFiles()?.forEach { child ->
            child.delete()
        }
    }

    private companion object {
        private const val SHARED_PREFS_FOLDER = "shared_prefs"
    }
}