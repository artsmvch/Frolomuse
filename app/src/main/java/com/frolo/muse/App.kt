package com.frolo.muse

import android.os.StrictMode
import androidx.core.content.ContextCompat
import androidx.multidex.MultiDexApplication
import com.frolo.muse.di.AppComponent
import com.frolo.muse.di.DaggerAppComponent
import com.frolo.muse.di.impl.navigator.NavigatorImpl
import com.frolo.muse.di.modules.*
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.PlayerWrapper
import com.frolo.muse.engine.service.PlayerService
import com.frolo.muse.navigator.NavigatorWrapper
import com.frolo.muse.logger.logAppLaunched
import com.frolo.muse.ui.base.BaseActivity
import com.frolo.muse.ui.base.FragmentNavigator
import com.frolo.muse.ui.main.MainActivity
import io.reactivex.plugins.RxJavaPlugins


class App : MultiDexApplication() {

    lateinit var appComponent: AppComponent
        private set

    private val lastStartedActivityCallback = LastStartedActivityWatcher()

    private val preferences by lazy { appComponent.providePreferences() }
    private val eventLogger by lazy { appComponent.provideEventLogger() }

    private val playerWrapper = PlayerWrapper()
    private val navigatorWrapper = NavigatorWrapper()

    override fun onCreate() {
        super.onCreate()

        appComponent = buildAppComponent()

        registerActivityLifecycleCallbacks(lastStartedActivityCallback)

        setupStrictMode()

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

        // Starting background player here, in the App class instance
        // so that the service will be also in created state when the MainActivity binds to the service.
        startBackgroundPlayer()

        dispatchAppLaunched()
    }

    private fun buildAppComponent(): AppComponent =
        DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .playerModule(PlayerModule(playerWrapper))
            .localDataModule(LocalDataModule())
            .remoteDataModule(RemoteDataModule())
            .navigationModule(NavigationModule(navigatorWrapper))
            .eventLoggerModule(EventLoggerModule(BuildConfig.DEBUG))
            .networkModule(NetworkModule())
            .miscModule(MiscModule())
            .build()

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
        val totalLaunchCount = preferences.openCount + 1 // +1 for the current launch
        preferences.openCount = totalLaunchCount
        eventLogger.logAppLaunched(totalLaunchCount)
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