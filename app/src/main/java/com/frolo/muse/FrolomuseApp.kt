package com.frolo.muse

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.StrictMode
import android.os.strictmode.Violation
import androidx.multidex.MultiDexApplication
import com.frolo.muse.broadcast.Broadcasts
import com.frolo.muse.di.modules.*
import com.frolo.player.PlayerImpl
import com.frolo.audiofx.AudioFxImpl
import com.frolo.debug.DebugUtils
import com.frolo.muse.di.*
import com.frolo.muse.logger.logLowMemory
import com.frolo.muse.ui.base.BaseActivity
import com.frolo.threads.HandlerExecutor
import com.frolo.threads.ThreadStrictMode
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import io.reactivex.plugins.RxJavaPlugins


class FrolomuseApp : MultiDexApplication(),
    ActivityWatcher,
    ApplicationComponentHolder {

    private lateinit var uiHandler: Handler

    private val preferences by lazy { applicationComponent.providePreferences() }
    private val eventLogger by lazy { applicationComponent.provideEventLogger() }
    private val activityWatcher by lazy { FrolomuseActivityWatcher(preferences, eventLogger) }

    override val applicationComponent: ApplicationComponent by lazy { buildApplicationComponent() }

    override fun onCreate() {
        super.onCreate()
        uiHandler = Handler(mainLooper)
        registerActivityLifecycleCallbacks(activityWatcher)
        setupDebugMode()
        setupStrictMode()
        setupRxPlugins()
        setupFirebaseRemoteConfigs()
        setupShortcutsListener()
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

    private fun setupDebugMode() {
        DebugUtils.setDebug(BuildConfig.DEBUG)
    }

    private fun setupStrictMode() {
        if (BuildInfo.isDebug()) {
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .run {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val executor = HandlerExecutor(mainLooper)
                        val listener = StrictMode.OnThreadViolationListener { violation ->
                            showViolation(violation)
                        }
                        penaltyListener(executor, listener)
                    } else {
                        this
                    }
                }
                .build()
                .also { StrictMode.setThreadPolicy(it) }

            StrictMode.VmPolicy.Builder()
                .detectActivityLeaks()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .detectLeakedRegistrationObjects()
                .setClassInstanceLimit(PlayerImpl::class.java, 1)
                .setClassInstanceLimit(AudioFxImpl::class.java, 1)
                .penaltyLog()
                .run {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val executor = HandlerExecutor(mainLooper)
                        val listener = StrictMode.OnVmViolationListener { violation ->
                            showViolation(violation)
                        }
                        penaltyListener(executor, listener)
                    } else {
                        this
                    }
                }
                .build()
                .also { StrictMode.setVmPolicy(it) }
        }
    }

    private fun showViolation(violation: Violation?) {
    }

    private fun setupRxPlugins() {
        RxJavaPlugins.setErrorHandler { err ->
            // Default error consumer
            eventLogger.log(err)
            runOnForegroundActivity { postError(err) }
        }
    }

    private fun setupFirebaseRemoteConfigs() {
        val instance = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(60 * 60 * 24) // 24 hours
            .build()
        instance.setConfigSettingsAsync(configSettings)
        instance.setDefaultsAsync(R.xml.firebase_remote_config_default)
    }

    private fun setupShortcutsListener() {
        val targetAction: String = Broadcasts.ACTION_SHORTCUT_PINNED
        // This callback will fire every time the user pinned an app shortcut
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent == null || intent.action != targetAction) {
                    return
                }

                runOnForegroundActivity { postMessage(getString(R.string.shortcut_created)) }
            }
        }
        val intentFilter = IntentFilter(targetAction)
        registerReceiver(receiver, intentFilter)
    }

    private fun runOnForegroundActivity(action: BaseActivity.() -> Unit) {
        uiHandler.post {
            ThreadStrictMode.assertMain()
            (activityWatcher.foregroundActivity as? BaseActivity)?.action()
        }
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

    override fun onLowMemory() {
        super.onLowMemory()
        eventLogger.logLowMemory()
        Logger.w(LOG_TAG, "Low memory!")
        if (BuildInfo.isDebug()) {
            runOnForegroundActivity {
                postMessage("Low memory!")
            }
        }
    }

    companion object {

        private const val LOG_TAG = "FrolomuseApp"

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