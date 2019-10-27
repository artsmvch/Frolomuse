package com.frolo.muse.ui.main

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.frolo.muse.R


fun Context.confirmDeletion(message: String, action: () -> Unit): Dialog {
    val listener = DialogInterface.OnClickListener { _, i ->
        if (i == DialogInterface.BUTTON_POSITIVE) action()
    }
    return AlertDialog.Builder(this)
            .setMessage(message)
            .setTitle(R.string.confirmation)
            .setIcon(R.drawable.ic_warning)
            .setPositiveButton(R.string.delete, listener)
            .setNegativeButton(R.string.cancel, listener)
            .show()
}