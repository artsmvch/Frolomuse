package com.frolo.audiofx.engine

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.frolo.audiofx.AudioFx2AttachInfo
import com.frolo.audiofx.audiosessions.AudioFx2AttachInfoHelper
import com.frolo.audiofx2.impl.AudioFx2Impl

class AudioFx2AttachEngine constructor(
    private val application: Application,
    private val audioFx2: AudioFx2Impl,
    private val attachInfoLiveData: MutableLiveData<AudioFx2AttachInfo>
) {
    fun attachToAudioSessionId(audioSessionId: Int, packageName: String?) {
        audioFx2.applyToAudioSession(audioSessionId)
        attachInfoLiveData.value =
            AudioFx2AttachInfoHelper.external(application, packageName, audioSessionId)
    }
}