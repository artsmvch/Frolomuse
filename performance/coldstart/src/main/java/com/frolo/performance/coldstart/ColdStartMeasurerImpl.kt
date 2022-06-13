package com.frolo.performance.coldstart

import android.app.Activity
import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.core.view.doOnPreDraw
import com.frolo.threads.ThreadUtils
import com.frolo.ui.SimpleActivityLifecycleCallbacks


internal class ColdStartMeasurerImpl : ContentProvider() {

    private var startTime: Long? = null
    private var wasRendered: Boolean = false

    private fun waitForFirstRender(context: Context) {
        val application = context.applicationContext as Application
        val callback = object : SimpleActivityLifecycleCallbacks() {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                waitForFirstRender(activity)
                ThreadUtils.runOnMainThread {
                    application.unregisterActivityLifecycleCallbacks(this)
                }
            }
        }
        application.registerActivityLifecycleCallbacks(callback)
    }

    private fun waitForFirstRender(activity: Activity) {
        val targetView = activity.window?.decorView ?: return
        targetView.doOnPreDraw {
            reportActivityRender(activity)
        }
    }

    private fun reportActivityRender(activity: Activity) {
        if (wasRendered) {
            return
        }
        wasRendered = true
        val startTime = this.startTime ?: return
        val endTime = System.currentTimeMillis()
        val elapsedTime = endTime - startTime
        val info = ColdStartInfo(
            firstRenderTime = elapsedTime
        )
        ColdStartMeasurer.report(info)
        Log.d(LOG_TAG, "First render time: $elapsedTime")
    }

    override fun onCreate(): Boolean {
        startTime = System.currentTimeMillis()
        context?.also(::waitForFirstRender)
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

    companion object {
        private const val LOG_TAG = "ColdStartMeasurerImpl"
    }
}