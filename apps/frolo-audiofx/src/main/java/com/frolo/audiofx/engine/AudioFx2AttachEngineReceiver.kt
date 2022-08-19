package com.frolo.audiofx.engine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.audiofx.AudioEffect
import android.media.audiofx.Equalizer
import com.frolo.audiofx.di.appComponent
import com.frolo.logger.api.Logger

class AudioFx2AttachEngineReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val audioSessionId = intent.getIntExtra(Equalizer.EXTRA_AUDIO_SESSION, -1)
        val packageName = intent.getStringExtra(Equalizer.EXTRA_PACKAGE_NAME)
        Logger.d(LOG_TAG, "Audio session ID caught! ID=$audioSessionId")
        appComponent.attachEngine.handleAudioSession(
            audioSessionId = audioSessionId,
            packageName = packageName
        )
    }

    companion object {
        private const val LOG_TAG = "AudioFx2AttachEngineReceiver"

        fun makeIntentFilter(): IntentFilter {
            return IntentFilter(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION)
        }
    }
}