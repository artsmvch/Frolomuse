package com.frolo.muse.di.modules

import android.content.Context
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.EventLoggerFactory
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class EventLoggerModule(private val debug: Boolean) {

    @Provides
    @Singleton
    fun provideEventLogger(context: Context): EventLogger {
        if (debug) {
            // We do not want to send any analytics for debug builds.
            // Debug builds are for developer, so Console logger is on.
            return EventLoggerFactory.createConsole()
        }

        // Currently, analytics is tracked using Firebase and Flurry (release builds only)
        return EventLoggerFactory.compose(
            EventLoggerFactory.createFlurry(context),
            EventLoggerFactory.createFirebase(context)
        )
    }

}