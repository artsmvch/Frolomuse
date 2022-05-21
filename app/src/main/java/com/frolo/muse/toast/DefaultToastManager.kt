package com.frolo.muse.toast

import android.app.Activity
import android.widget.Toast

class DefaultToastManager(
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