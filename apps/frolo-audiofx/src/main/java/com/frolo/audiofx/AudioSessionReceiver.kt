package com.frolo.audiofx

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.audiofx.Equalizer
import android.widget.Toast
import com.frolo.audiofx.audiosessions.AudioFx2AttachInfoHelper
import com.frolo.audiofx.di.appComponent
import com.frolo.logger.api.Logger

class AudioSessionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val audioSessionId = intent.getIntExtra(Equalizer.EXTRA_AUDIO_SESSION, -1)
        val packageName = intent.getStringExtra(Equalizer.EXTRA_PACKAGE_NAME)
        Logger.d("AudioSessionReceiver", "audioSessionId=$audioSessionId")
        if (audioSessionId > 0) {
            appComponent.audioFx2.applyToAudioSession(audioSessionId)
            Toast.makeText(context, "Applied Audio effects!", Toast.LENGTH_LONG).show()
        }
        appComponent.audioFx2AttachInfo.value =
            AudioFx2AttachInfoHelper.external(context, packageName, audioSessionId)
    }
}