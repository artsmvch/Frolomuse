package com.frolo.muse

import android.app.Activity
import android.content.Context
import androidx.multidex.MultiDexApplication
import com.frolo.muse.di.modules.*
import com.frolo.muse.di.*


class FrolomuseApp : MultiDexApplication(),
    ActivityWatcher,
    ApplicationComponentHolder {

    private val activityWatcher by lazy { ActivityWatcherImpl() }

    override val applicationComponent: ApplicationComponent by lazy { buildApplicationComponent() }

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(activityWatcher)
        applicationComponent.provideApplicationStartUp().init()
    }

    private fun buildApplicationComponent(): ApplicationComponent {
        return DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(this))
            .localDataModule(LocalDataModule())
            .remoteDataModule(RemoteDataModule())
            .miscModule(MiscModule(BuildInfo.isDebug()))
            .billingModule(BillingModule(BuildInfo.isDebug()))
            .build()
    }

    //region Activity watcher
    override fun getCreatedActivities(): List<Activity> {
        return activityWatcher.createdActivities
    }

    override fun getStartedActivities(): List<Activity> {
        return activityWatcher.startedActivities
    }

    override fun getResumedActivities(): List<Activity> {
        return activityWatcher.resumedActivities
    }

    override fun getForegroundActivity(): Activity? {
        return activityWatcher.foregroundActivity
    }
    //endregion

    companion object {
        fun from(context: Context): FrolomuseApp {
            val applicationContext: Context = context.applicationContext
            if (applicationContext !is FrolomuseApp) {
                throw NullPointerException("Application context is not an instance " +
                        "of ${FrolomuseApp::class.java.simpleName}")
            }
            return applicationContext
        }
    }

}