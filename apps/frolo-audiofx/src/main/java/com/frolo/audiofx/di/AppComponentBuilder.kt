package com.frolo.audiofx.di

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.frolo.audiofx.AudioFx2AttachInfo
import com.frolo.audiofx.audiosessions.AudioFx2AttachInfoHelper
import com.frolo.audiofx.engine.AudioFx2AttachEngine
import com.frolo.audiofx2.impl.AudioEffect2ErrorHandler
import com.frolo.audiofx2.impl.AudioFx2Impl

internal class AppComponentImpl(
    private val application: Application
) : AppComponent {
    override val audioFx2: AudioFx2Impl by lazy {
        val errorHandler = AudioEffect2ErrorHandler { _, err ->
            Toast.makeText(application, "Error: $err", Toast.LENGTH_LONG).show()
        }
        AudioFx2Impl.obtain(application, errorHandler)
    }

    override val audioFx2AttachInfo: MutableLiveData<AudioFx2AttachInfo> by lazy {
        MutableLiveData<AudioFx2AttachInfo>(AudioFx2AttachInfoHelper.default(application))
    }

    override val attachEngine: AudioFx2AttachEngine by lazy {
        AudioFx2AttachEngine(application, audioFx2, audioFx2AttachInfo)
    }
}