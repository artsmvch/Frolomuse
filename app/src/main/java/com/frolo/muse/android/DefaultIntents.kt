@file:Suppress("FunctionName")

package com.frolo.muse.android

import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.provider.Settings
import com.frolo.muse.OS
import java.io.File


fun DisplayAudioEffectControlPanelIntent(packageName: String, audioSessionId: Int): Intent {
    return Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
        .putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)
        .putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId)
        .putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
}

fun SendTextFileIntent(context: Context, file: File): Intent {
    val intent = Intent(Intent.ACTION_SEND)
    if (OS.isAtLeastN()) {
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val uri = context.resolveUri(file)
    intent.putExtra(Intent.EXTRA_STREAM, uri)
    intent.type = "text/*"
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return intent
}

fun getLinkToAppPageInPlayStore(packageName: String): String {
    return "http://play.google.com/store/apps/details?id=$packageName"
}

fun ViewAppInPlayStoreIntent(context: Context): Intent {
    val link = getLinkToAppPageInPlayStore(context.packageName)
    val uri = Uri.parse(link)
    return Intent(Intent.ACTION_VIEW, uri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
    }
}

fun ViewAppInDefaultStore(context: Context): Intent {
    val packageName = context.packageName
    val uri = Uri.parse("market://details?id=$packageName")
    return Intent(Intent.ACTION_VIEW, uri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
    }
}

fun ViewAppInStoreIntent(context: Context): Intent {
    val viewAppInPlayStoreIntent = ViewAppInPlayStoreIntent(context)
    // At first we want to view it in PlayStore
    return if (context.canStartActivity(viewAppInPlayStoreIntent)) {
        viewAppInPlayStoreIntent
    } else {
        // If we cannot view it in PlayStore,
        // then we search for default app market.
        ViewAppInDefaultStore(context)
    }
}

fun ViewAppSettingsIntent(context: Context): Intent {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    intent.data = Uri.fromParts("package", context.packageName, null)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return intent
}