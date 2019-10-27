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
        return if (debug) {
            EventLoggers.compose(
                    EventLoggers.createFirebase(),
                    EventLoggers.createDroid()
            )
        } else {
            EventLoggers.compose(
                    EventLoggers.createFlurry(context),
                    EventLoggers.createCrashlytics(context),
                    EventLoggers.createFirebase()
            )
        }
    }

}