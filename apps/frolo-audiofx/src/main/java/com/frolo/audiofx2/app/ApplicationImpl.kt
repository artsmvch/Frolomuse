package com.frolo.audiofx2.app

import android.app.Application
import androidx.lifecycle.LiveData
import com.frolo.audiofx.app.BuildConfig
import com.frolo.audiofx2.ui.AudioFx2AttachInfo
import com.frolo.audiofx2.ui.AudioFx2Feature
import com.frolo.audiofx2.ui.AudioFx2FeatureInput
import com.frolo.audiofx2.app.di.AppComponentImpl
import com.frolo.audiofx2.app.di.appComponent
import com.frolo.audiofx2.app.di.initAppComponent
import com.frolo.audiofx2.AudioFx2
import com.frolo.logger.api.CompositeLogDelegate
import com.frolo.logger.api.LogDelegate
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
        val logDelegates = ArrayList<LogDelegate>(2).apply {
            if (BuildConfig.GOOGLE_SERVICES_ENABLED) {
                add(FirebaseLogDelegate())
            }
            if (BuildConfig.DEBUG) {
                add(ConsoleLogDelegate())
            }
        }
        Logger.init(
            LoggerParams(
                logDelegate = CompositeLogDelegate(
                    delegates = logDelegates
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