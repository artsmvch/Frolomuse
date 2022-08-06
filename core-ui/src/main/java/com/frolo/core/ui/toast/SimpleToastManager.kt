package com.frolo.core.ui.toast

import android.app.Activity
import android.widget.Toast

internal class SimpleToastManager(
    private val activity: Activity
) : ToastManager {
    override fun showToastMessage(message: CharSequence) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    override fun showToastMessage(error: Throwable) {
        Toast.makeText(activity, error.message ?: "NULL",
            Toast.LENGTH_SHORT).show()
    }
}