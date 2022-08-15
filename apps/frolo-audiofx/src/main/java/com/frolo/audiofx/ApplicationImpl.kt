package com.frolo.audiofx

import android.app.Application
import com.frolo.audiofx.di.AppComponentImpl
import com.frolo.audiofx.di.appComponent
import com.frolo.audiofx.di.initAppComponent
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
            audioFx2Provider = { appComponent.audioFx2 },
            audioSessionDescription = appComponent.audioSessionDescription
        )
    }
}