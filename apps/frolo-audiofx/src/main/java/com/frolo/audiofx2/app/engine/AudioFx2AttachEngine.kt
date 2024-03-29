package com.frolo.audiofx2.app.engine

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import com.frolo.audiofx2.ui.AudioFx2AttachInfo
import com.frolo.audiofx2.app.attachinfo.AudioFx2AttachInfoHelper
import com.frolo.audiofx2.impl.AudioFx2AttachTarget
import com.frolo.audiofx2.impl.AudioFx2Impl
import java.util.concurrent.atomic.AtomicReference

class AudioFx2AttachEngine constructor(
    private val application: Application,
    private val audioFx2: AudioFx2Impl,
    private val attachInfoLiveData: MutableLiveData<AudioFx2AttachInfo>
) {
    private val prefs: SharedPreferences by lazy {
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    private val lastAudioSessionInfoRef = AtomicReference<AudioSessionInfo?>()

    @Synchronized
    fun isEnabled(): Boolean {
        return prefs.getBoolean(KEY_ENABLED, true)
    }

    @Synchronized
    fun setEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_ENABLED, enabled) }
        if (enabled) {
            lastAudioSessionInfoRef.get()?.also(::attachImpl)
        } else {
            audioFx2.release()
        }
    }

    @Synchronized
    fun handleAudioSession(audioSessionId: Int, packageName: String?) {
        val audioSessionInfo = AudioSessionInfo(audioSessionId, packageName)
        if (isEnabled()) {
            attachImpl(audioSessionInfo)
        }
        lastAudioSessionInfoRef.set(audioSessionInfo)
    }

    @Synchronized
    private fun attachImpl(audioSessionInfo: AudioSessionInfo) {
        val attachTarget = AudioFx2AttachTarget(
            priority = 0,
            sessionId = audioSessionInfo.sessionId,
            mediaPlayer = null
        )
        audioFx2.attachTo(attachTarget)
        attachInfoLiveData.postValue(
            AudioFx2AttachInfoHelper.external(application,
                audioSessionInfo.packageName, audioSessionInfo.sessionId)
        )
    }

    @Synchronized
    fun releaseAudioSession(audioSessionId: Int, packageName: String?) {
        val lastAudioSessionInfo = lastAudioSessionInfoRef.get()
        if (lastAudioSessionInfo != null && lastAudioSessionInfo.sessionId == audioSessionId) {
            audioFx2.release()
            lastAudioSessionInfoRef.set(null)
            attachInfoLiveData.postValue(
                AudioFx2AttachInfoHelper.default(application)
            )
        }
    }

    private class AudioSessionInfo(
        val sessionId: Int,
        val packageName: String?
    )

    companion object {
        private const val PREFS_NAME = "com.frolo.audiofx2.attach_engine"

        private const val KEY_ENABLED = "enabled"
    }
}