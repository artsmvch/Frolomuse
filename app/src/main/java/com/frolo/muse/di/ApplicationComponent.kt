package com.frolo.muse.di

import android.app.Activity
import android.app.Service
import android.content.Context
import com.frolo.muse.di.modules.*
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.repository.FirebasePreferences
import com.frolo.muse.repository.Preferences
import com.frolo.muse.startup.AppStartUpInitializer
import com.frolo.muse.ui.base.BaseActivity
import com.frolo.music.repository.SongRepository
import dagger.Component
import java.lang.IllegalArgumentException


@ApplicationScope
@Component(
    modules = [
        ApplicationModule::class,
        LocalDataModule::class,
        RemoteDataModule::class,
        BillingModule::class,
        MiscModule::class
    ]
)
interface ApplicationComponent {
    fun activityComponent(
        activityModule: ActivityModule
    ): ActivityComponent

    fun serviceComponent(
        serviceModule: ServiceModule
    ): ServiceComponent

    fun inject(activity: BaseActivity)

    fun provideAppStartUpInitializer(): AppStartUpInitializer
    fun provideSongRepository(): SongRepository
    fun providePreferences(): Preferences
    fun provideEventLogger(): EventLogger
    fun provideFirebasePreferences(): FirebasePreferences
}

interface ApplicationComponentHolder {
    val applicationComponent: ApplicationComponent
}

val Context.applicationComponent: ApplicationComponent
    get() {
        val safeApplication = when (this) {
            is Activity -> application
            is Service -> application
            else -> applicationContext
        } ?: throw NullPointerException("$this not attached to an Application")

        if (safeApplication !is ApplicationComponentHolder) {
            throw IllegalArgumentException("$safeApplication is not a component holder")
        }

        return safeApplication.applicationComponent
    }