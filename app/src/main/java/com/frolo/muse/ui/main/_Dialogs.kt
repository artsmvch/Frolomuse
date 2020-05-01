package com.frolo.muse.ui.main

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import com.frolo.muse.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder


fun Context.confirmDeletion(message: String, whenConfirmed: () -> Unit): Dialog {
    val listener = DialogInterface.OnClickListener { _, i ->
        if (i == DialogInterface.BUTTON_POSITIVE) whenConfirmed.invoke()
    }
    return MaterialAlertDialogBuilder(this)
        .setMessage(message)
        .setTitle(R.string.confirmation)
        .setIcon(R.drawable.ic_warning)
        .setPositiveButton(R.string.delete, listener)
        .setNegativeButton(R.string.cancel, listener)
        .show()
}