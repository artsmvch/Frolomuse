package com.frolo.performance.anr

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Looper


internal class DefaultAnrListener(
    private val uiContextProvider: () -> Context?
) : OnAnrDetectedListener {

    override fun onAnrDetected(looper: Looper, info: AnrInfo) {
        val uiContext = uiContextProvider.invoke() ?: return
        val uiHandler = Handler(uiContext.mainLooper)
        uiHandler.post {
            createDialog(uiContext, info).show()
        }
    }

    private fun createDialog(context: Context, info: AnrInfo): Dialog {
        return AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
            .setMessage("ANR detected: ${info.toStringDetailed()}")
            .setTitle("ANR detected")
            .setPositiveButton(android.R.string.ok, null)
            .create()
    }
}