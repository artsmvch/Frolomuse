package com.frolo.muse.di.modules

import android.content.Context
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.EventLoggers
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
            // Debug builds are for developer, so Droid logger is on.
            return EventLoggers.createDroid()
        }

        // Currently, analytics is tracked using Firebase and Flurry (release builds only)
        return EventLoggers.compose(
            EventLoggers.createFlurry(context),
            EventLoggers.createFirebase(context)
        )
    }

}