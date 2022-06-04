package com.frolo.core.ui.dialog

import android.content.Context

object AlertDialogBuilders {
    fun newBuilder(context: Context): AlertDialogBuilder {
        return DefaultAlertDialogBuilder(context)
    }
}