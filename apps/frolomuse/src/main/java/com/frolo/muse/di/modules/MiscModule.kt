package com.frolo.muse.di.modules

import android.app.Application
import android.content.Context
import com.frolo.muse.battery.BatteryOptimizationSettings
import com.frolo.muse.battery.BatteryOptimizationSettingsImpl
import com.frolo.muse.billing.TrialManager
import com.frolo.muse.billing.TrialManagerImpl
import com.frolo.muse.di.ApplicationScope
import com.frolo.muse.di.ExecutorQualifier
import com.frolo.muse.di.impl.misc.MainExecutor
import com.frolo.muse.di.impl.network.NetworkHelperImpl
import com.frolo.muse.di.impl.permission.PermissionCheckerImpl
import com.frolo.muse.di.impl.rx.SchedulerProviderImpl
import com.frolo.muse.links.AppLinksProcessor
import com.frolo.muse.links.LinksProcessor
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.EventLoggerFactory
import com.frolo.muse.network.NetworkHelper
import com.frolo.muse.permission.PermissionChecker
import com.frolo.muse.player.journals.AndroidLogPlayerJournal
import com.frolo.muse.player.journals.CompositePlayerJournal
import com.frolo.muse.player.journals.StoredInMemoryPlayerJournal
import com.frolo.muse.router.AppRouter
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.player.PlayerJournal
import dagger.Module
import dagger.Provides
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import com.frolo.muse.BuildInfo


@Module
class MiscModule {

    @ApplicationScope
    @Provides
    fun provideSchedulers(): SchedulerProvider {
        return SchedulerProviderImpl()
    }

    @Provides
    @ApplicationScope
    fun provideEventLogger(context: Context): EventLogger {
        val eventLoggers = ArrayList<EventLogger>(2)
        if (BuildInfo.isDebug()) {
            // We do not want to send any analytics for debug builds.
            // Debug builds are for developer, so Console logger is on.
            eventLoggers.add(EventLoggerFactory.createConsole())
        }
        if (BuildInfo.isFirebaseEnabled()) {
            // Analytics is tracked using Firebase (production builds only)
            eventLoggers.add(EventLoggerFactory.createFirebase(context))
        }
        return EventLoggerFactory.compose(eventLoggers)
    }

    @ApplicationScope
    @Provides
    @ExecutorQualifier(ExecutorQualifier.Type.MAIN)
    fun provideMainExecutor(): Executor {
        return MainExecutor()
    }

    @ApplicationScope
    @Provides
    @ExecutorQualifier(ExecutorQualifier.Type.QUERY)
    fun provideQueryExecutor(): Executor {
        return Executors.newCachedThreadPool()
    }

    @ApplicationScope
    @Provides
    fun providePermissionChecker(context: Context): PermissionChecker {
        return PermissionCheckerImpl(context)
    }

    @Provides
    @ApplicationScope
    fun provideTrialManager(context: Context): TrialManager {
        return TrialManagerImpl(context)
    }

    @ApplicationScope
    @Provides
    fun providePlayerJournal(): PlayerJournal {
        if (BuildInfo.isDebug()) {
            val journals = listOf(
                AndroidLogPlayerJournal("FrolomusePlayerJournal"),
                StoredInMemoryPlayerJournal()
            )
            return CompositePlayerJournal(journals)
        }

        return PlayerJournal.EMPTY
    }

    @ApplicationScope
    @Provides
    fun provideNetworkHelper(context: Context): NetworkHelper {
        return NetworkHelperImpl(context)
    }

    @ApplicationScope
    @Provides
    fun provideLinksProcessor(router: AppRouter): LinksProcessor {
        return AppLinksProcessor(router)
    }

    @ApplicationScope
    @Provides
    fun provideBatteryOptimizationSettings(application: Application): BatteryOptimizationSettings {
        return BatteryOptimizationSettingsImpl(application)
    }

}