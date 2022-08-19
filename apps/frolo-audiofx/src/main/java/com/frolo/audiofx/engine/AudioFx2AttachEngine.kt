package com.frolo.audiofx.engine

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import com.frolo.audiofx.AudioFx2AttachInfo
import com.frolo.audiofx.audiosessions.AudioFx2AttachInfoHelper
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
    private val lastAudioSessionInfoRef = AtomicReference<AudioSessionInfo>()

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
        audioFx2.applyToAudioSession(audioSessionInfo.sessionId)
        attachInfoLiveData.value = AudioFx2AttachInfoHelper.external(
            application, audioSessionInfo.packageName, audioSessionInfo.sessionId)
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