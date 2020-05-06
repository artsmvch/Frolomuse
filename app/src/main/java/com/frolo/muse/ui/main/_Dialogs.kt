package com.frolo.muse.ui.main

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.text.Html
import com.frolo.muse.R
import com.frolo.muse.model.media.Media
import com.frolo.muse.ui.getName
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

fun Context.confirmShortcutCreation(media: Media, whenConfirmed: () -> Unit): Dialog {
    val listener = DialogInterface.OnClickListener { _, i ->
        if (i == DialogInterface.BUTTON_POSITIVE) whenConfirmed.invoke()
    }

    // Here we try to apply a bold span to a parameterized string from res
    val message = try {
        getString(R.string.do_you_want_to_create_shortcut_for_s).let { str ->
            val param = media.getName()

            // TODO: find out why isn't the bold spannable working in here
//            val index = str.indexOf("%s")
//            val len = param.length
//            val resultStr = str.replace("%s", param)
//
//            val sp = SpannableString(resultStr)
//            sp.setSpan(StyleSpan(Typeface.BOLD), index, index + len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//
//            return@let resultStr

            val resultStr = str.replace("%s", "<b>$param</b>")

            return@let if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(resultStr, Html.FROM_HTML_MODE_LEGACY)
            } else {
                Html.fromHtml(resultStr)
            }
        }
    } catch (ignored: Throwable) {
        getString(R.string.do_you_want_to_create_shortcut_for_s, media.getName())
    }

    return MaterialAlertDialogBuilder(this)
        .setMessage(message)
        .setTitle(R.string.create_shortcut)
        .setIcon(R.drawable.ic_shortcut_18dp)
        .setPositiveButton(R.string.create, listener)
        .setNegativeButton(R.string.cancel, listener)
        .show()
}