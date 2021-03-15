@file:Suppress("FunctionName")

package com.frolo.muse.ui

import android.content.Intent
import android.media.audiofx.AudioEffect


fun DisplayAudioEffectControlPanelIntent(packageName: String, audioSessionId: Int): Intent {
    return Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
        .putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)
        .putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId)
        .putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
}