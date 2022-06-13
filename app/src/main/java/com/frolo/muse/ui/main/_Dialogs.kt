package com.frolo.muse.ui.main

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.text.Html
import com.frolo.muse.R
import com.frolo.muse.model.event.DeletionConfirmation
import com.frolo.muse.model.event.DeletionType
import com.frolo.muse.model.event.MultipleDeletionConfirmation
import com.frolo.music.model.Media
import com.frolo.music.model.Playlist
import com.frolo.music.model.Song
import com.frolo.muse.ui.getDeletionConfirmationMessage
import com.frolo.muse.ui.getName
import com.google.android.material.dialog.MaterialAlertDialogBuilder


fun <E: Media> Context.confirmDeletion(
    confirmation: DeletionConfirmation<E>,
    onConfirmed: (DeletionType) -> Unit
): Dialog {

    val item = confirmation.mediaItem
    val associatedMedia = confirmation.associatedMediaItem

    val message = getDeletionConfirmationMessage(item)

    val builder = MaterialAlertDialogBuilder(this)
        .setMessage(message)
        .setTitle(R.string.confirmation)
        .setIcon(R.drawable.ic_warning)

    if (item is Song && associatedMedia is Playlist) {
        val listener = DialogInterface.OnClickListener { _, i ->
            if (i == DialogInterface.BUTTON_POSITIVE) {
                onConfirmed.invoke(DeletionType.FromAssociatedMedia(associatedMedia))
            } else if (i == DialogInterface.BUTTON_NEGATIVE) {
                onConfirmed.invoke(DeletionType.FromDevice)
            }
        }
        builder.setPositiveButton(R.string.delete_from_playlist, listener)
            .setNegativeButton(R.string.delete_from_device, listener)
            .setNeutralButton(R.string.cancel, listener)
    } else {
        val listener = DialogInterface.OnClickListener { _, i ->
            if (i == DialogInterface.BUTTON_POSITIVE) {
                onConfirmed.invoke(DeletionType.FromDevice)
            }
        }
        builder.setPositiveButton(R.string.delete, listener)
            .setNegativeButton(R.string.cancel, listener)
    }

    return builder.show()
}

fun <E: Media> Context.confirmDeletion(
    confirmation: MultipleDeletionConfirmation<E>,
    onConfirmed: (DeletionType) -> Unit
): Dialog {

    val items = confirmation.mediaItems
    val associatedMedia = confirmation.associatedMediaItem

    val message = getDeletionConfirmationMessage(items)

    val builder = MaterialAlertDialogBuilder(this)
        .setMessage(message)
        .setTitle(R.string.confirmation)
        .setIcon(R.drawable.ic_warning)

    if (items.all { item -> item is Song } && associatedMedia is Playlist) {
        val listener = DialogInterface.OnClickListener { _, i ->
            if (i == DialogInterface.BUTTON_POSITIVE) {
                onConfirmed.invoke(DeletionType.FromAssociatedMedia(associatedMedia))
            } else if (i == DialogInterface.BUTTON_NEGATIVE) {
                onConfirmed.invoke(DeletionType.FromDevice)
            }
        }
        builder.setPositiveButton(R.string.delete_from_playlist, listener)
            .setNegativeButton(R.string.delete_from_device, listener)
            .setNeutralButton(R.string.cancel, listener)
    } else {
        val listener = DialogInterface.OnClickListener { _, i ->
            if (i == DialogInterface.BUTTON_POSITIVE) {
                onConfirmed.invoke(DeletionType.FromDevice)
            }
        }
        builder.setPositiveButton(R.string.delete, listener)
            .setNegativeButton(R.string.cancel, listener)
    }

    return builder.show()
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

fun Context.informAboutAnr(responseTime: Long): Dialog {
    return MaterialAlertDialogBuilder(this)
        .setMessage("ANR detected: response time is $responseTime millis")
        .setTitle("ANR detected")
        .setPositiveButton(R.string.ok, null)
        .show()
}