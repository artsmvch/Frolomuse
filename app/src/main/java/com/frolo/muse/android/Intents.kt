@file:Suppress("FunctionName")

package com.frolo.muse.android

import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
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