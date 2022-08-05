package com.frolo.core.ui

import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.frolo.core.ui.activity.ActivityWatcher
import com.frolo.core.ui.activity.ActivityWatcherImpl

internal class ApplicationWatcherImpl: ContentProvider() {
    private lateinit var activityWatcherRef: ActivityWatcher
    val activityWatcher: ActivityWatcher get() = activityWatcherRef

    override fun onCreate(): Boolean {
        Log.d(LOG_TAG, "Creating...")
        instanceRef = this
        activityWatcherRef = ActivityWatcherImpl().also { watcherImpl ->
            requireApplicationContext().registerActivityLifecycleCallbacks(watcherImpl)
        }
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0

    fun requireApplicationContext(): Application {
        return this.context!!.applicationContext as Application
    }

    companion object {
        private const val LOG_TAG = "ApplicationWatcherImpl"

        private var instanceRef: ApplicationWatcherImpl? = null
        val instance: ApplicationWatcherImpl get() {
            return instanceRef ?: throw NullPointerException(
                "ApplicationWatcherImpl has not been created yet")
        }
    }
}