package com.frolo.muse

import android.os.StrictMode
import androidx.core.content.ContextCompat
import androidx.multidex.MultiDexApplication
import com.crashlytics.android.Crashlytics
import com.frolo.muse.di.AppComponent
import com.frolo.muse.di.DaggerAppComponent
import com.frolo.muse.di.impl.navigator.NavigatorImpl
import com.frolo.muse.di.modules.*
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.PlayerWrapper
import com.frolo.muse.engine.service.PlayerService
import com.frolo.muse.navigator.NavigatorWrapper
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.repository.Preferences
import com.frolo.muse.ui.base.BaseActivity
import com.frolo.muse.ui.base.FragmentNavigator
import com.frolo.muse.ui.main.MainActivity
import com.frolo.muse.util.Mapper
import io.fabric.sdk.android.Fabric
import io.reactivex.plugins.RxJavaPlugins
import javax.inject.Inject


class App : MultiDexApplication() {

    lateinit var appComponent: AppComponent
        private set

    private val lastStartedActivityCallback = LastStartedActivityWatcher()

    @Inject
    lateinit var preferences: Preferences
    @Inject
    lateinit var eventLogger: EventLogger

    private val playerWrapper = PlayerWrapper()
    private val navigatorWrapper = NavigatorWrapper()

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .playerModule(PlayerModule(playerWrapper))
                .localDataModule(LocalDataModule())
                .remoteDataModule(RemoteDataModule())
                .navigationModule(NavigationModule(navigatorWrapper))
                .eventLoggerModule(EventLoggerModule(BuildConfig.DEBUG))
                .networkModule(NetworkModule())
                .miscModule(MiscModule())
                .build()

        registerActivityLifecycleCallbacks(lastStartedActivityCallback)

        appComponent.inject(this)

        setupStrictMode()

        // consider initializing crashlytics in Release version only
        Fabric.with(this, Crashlytics())

        // init Rx plugins
        RxJavaPlugins.setErrorHandler { err ->
            // Default error consumer
            eventLogger.log(err)
            (lastStartedActivityCallback.currentLast as? BaseActivity)?.let { activity ->
                activity.runOnUiThread {
                    activity.postError(err)
                }
            }
        }

        // starting background player in the App class;
        // so it will be created when the Main activity binds to the service
        startBackgroundPlayer()

        dispatchAppLaunched()
    }

    private fun setupStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build())
        }
    }

    private fun dispatchAppLaunched() {
        preferences.openCount += 1
        eventLogger.also { logger ->
            val params = Mapper.map(
                    EventLogger.PARAM_COUNT,
                    preferences.openCount.toString())
            logger.log(EventLogger.EVENT_APP_LAUNCHED, params)
        }
    }

    private fun startBackgroundPlayer() {
        val intent = PlayerService.newIntent(this)
        ContextCompat.startForegroundService(this, intent)
    }

    fun onPlayerConnected(player: Player) {
        playerWrapper.attachOrigin(player)
    }

    fun onPlayerDisconnected() {
        playerWrapper.detachOrigin()
    }

    fun onFragmentNavigatorCreated(fragmentNavigator: FragmentNavigator) {
        navigatorWrapper.attachOrigin(
                NavigatorImpl(fragmentNavigator as MainActivity))
    }

    fun onFragmentNavigatorDestroyed() {
        navigatorWrapper.detachOrigin()
    }
}