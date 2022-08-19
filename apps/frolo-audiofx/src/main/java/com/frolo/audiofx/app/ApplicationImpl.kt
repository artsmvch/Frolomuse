package com.frolo.audiofx.app

import android.app.Application
import androidx.lifecycle.LiveData
import com.frolo.audiofx.AudioFx2AttachInfo
import com.frolo.audiofx.AudioFx2Feature
import com.frolo.audiofx.AudioFx2FeatureInput
import com.frolo.audiofx.app.di.AppComponentImpl
import com.frolo.audiofx.app.di.appComponent
import com.frolo.audiofx.app.di.initAppComponent
import com.frolo.audiofx2.AudioFx2
import com.frolo.logger.api.CompositeLogDelegate
import com.frolo.logger.api.Logger
import com.frolo.logger.api.LoggerParams
import com.frolo.logger.impl.ConsoleLogDelegate
import com.frolo.logger.impl.FirebaseLogDelegate

class ApplicationImpl : Application() {
    override fun onCreate() {
        super.onCreate()
        setup()
    }

    private fun setup() {
        initAppComponent(AppComponentImpl(this))
        Logger.init(
            LoggerParams(
                logDelegate = CompositeLogDelegate(
                    delegates = listOf(
                        ConsoleLogDelegate(),
                        FirebaseLogDelegate()
                    )
                )
            )
        )
        AudioFx2Feature.init(
            input = object : AudioFx2FeatureInput {
                override val audioFx2: AudioFx2 get() = appComponent.audioFx2
                override val audioFx2AttachInfo: LiveData<AudioFx2AttachInfo>
                    get() = appComponent.audioFx2AttachInfo
            }
        )
    }
}