package com.frolo.audiofx2.app.di

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.frolo.audiofx2.ui.AudioFx2AttachInfo
import com.frolo.audiofx2.app.attachinfo.AudioFx2AttachInfoHelper
import com.frolo.audiofx2.app.engine.AudioFx2AttachEngine
import com.frolo.audiofx2.impl.AudioEffect2ErrorHandler
import com.frolo.audiofx2.impl.AudioFx2Impl
import com.frolo.audiofx2.impl.BuildConfig
import com.frolo.logger.api.Logger
import com.google.firebase.crashlytics.FirebaseCrashlytics

internal class AppComponentImpl(
    private val application: Application
) : AppComponent {
    override val audioFx2: AudioFx2Impl by lazy {
        val errorHandler = AudioEffect2ErrorHandler { _, err ->
            FirebaseCrashlytics.getInstance().recordException(err)
            Logger.e("AudioFx2ErrorHandler", err)
            if (BuildConfig.DEBUG) {
                Toast.makeText(application, "Error: $err", Toast.LENGTH_LONG).show()
            }
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