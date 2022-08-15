package com.frolo.audiofx

import android.app.Application
import androidx.lifecycle.LiveData
import com.frolo.audiofx.di.AppComponentImpl
import com.frolo.audiofx.di.appComponent
import com.frolo.audiofx.di.initAppComponent
import com.frolo.audiofx2.AudioFx2
import com.frolo.logger.api.Logger
import com.frolo.logger.api.LoggerParams
import com.frolo.logger.impl.ConsoleLogDelegate

class ApplicationImpl : Application() {
    override fun onCreate() {
        super.onCreate()
        initAppComponent(AppComponentImpl(this))
        Logger.init(
            LoggerParams(
                logDelegate = ConsoleLogDelegate()
            )
        )
        AudioFx2Feature.init(
            input = object : AudioFx2FeatureInput {
                override val audioFx2: AudioFx2 get() = appComponent.audioFx2
                override val audioSessionDescription: LiveData<AudioSessionDescription>
                    get() = appComponent.audioSessionDescription
            }
        )
    }
}