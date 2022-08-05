package com.frolo.muse.startup

import android.app.Activity
import android.content.*
import android.content.res.Configuration
import android.os.Build
import android.os.Handler
import android.os.StrictMode
import android.os.strictmode.Violation
import androidx.annotation.UiThread
import com.frolo.audiofx.AudioFxImpl
import com.frolo.core.ui.ApplicationWatcher
import com.frolo.debug.DebugUtils
import com.frolo.muse.*
import com.frolo.muse.broadcast.Broadcasts
import com.frolo.muse.di.ApplicationScope
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.logAppLaunched
import com.frolo.muse.logger.logLowMemory
import com.frolo.mediascan.scheduleMediaScanWork
import com.frolo.muse.player.PlayerWrapper
import com.frolo.muse.memory.MemoryWatcherRegistryImpl
import com.frolo.muse.repository.Preferences
import com.frolo.muse.ui.base.BaseActivity
import com.frolo.performance.anr.AnrDetectors
import com.frolo.performance.anr.OnAnrDetectedListener
import com.frolo.performance.coldstart.ColdStartMeasurer
import com.frolo.player.PlayerImpl
import com.frolo.threads.HandlerExecutor
import com.frolo.threads.ThreadStrictMode
import com.frolo.ui.SimpleActivityLifecycleCallbacks
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import io.reactivex.plugins.RxJavaPlugins
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@ApplicationScope
class AppStartUpInitializer @Inject constructor(
    private val application: FrolomuseApp,
    private val preferences: Preferences,
    private val eventLogger: EventLogger
) {

    private var isInitialized = false

    private var mainThreadHandler: Handler? = null

    private val componentCallbacks = object : ComponentCallbacks {
        override fun onConfigurationChanged(newConfig: Configuration) {
        }

        override fun onLowMemory() {
            noteLowMemory()
        }
    }

    private val activityLifecycleCallbacks = object : SimpleActivityLifecycleCallbacks() {
        private var activityResumeCount: Int = 0

        override fun onActivityResumed(activity: Activity) {
            if (activityResumeCount == 0) {
                // This is the first resume, we consider it as the actual launch of the app
                noteAppLaunch()
            }
            activityResumeCount++
        }
    }

    @UiThread
    fun init() {
        ThreadStrictMode.assertMain()
        if (isInitialized) {
            val error = IllegalStateException("Start Up already initialized")
            DebugUtils.dumpOnMainThread(error)
            return
        }
        val startTimeMillis = System.currentTimeMillis()
        initImpl()
        isInitialized = true
        val elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis
        Logger.d(LOG_TAG, "Start Up initialized in $elapsedTimeMillis millis")
    }

    private fun initImpl() {
        mainThreadHandler = Handler(application.mainLooper)
        application.registerComponentCallbacks(componentCallbacks)
        application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
        setupDebugMode()
        setupStrictMode()
        setupPerformanceMetrics()
        setupRxPlugins()
        setupFirebase()
        setupShortcutsListener()
        //setupMediaScanWork()
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
                        val executor = HandlerExecutor(application.mainLooper)
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
                .setClassInstanceLimit(AppStartUpInitializer::class.java, 1)
                .setClassInstanceLimit(PlayerImpl::class.java, 1)
                .setClassInstanceLimit(AudioFxImpl::class.java, 1)
                .setClassInstanceLimit(PlayerWrapper::class.java, 1)
                .setClassInstanceLimit(MemoryWatcherRegistryImpl::class.java, 1)
                .penaltyLog()
                .run {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val executor = HandlerExecutor(application.mainLooper)
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

    private fun setupPerformanceMetrics() {
        ColdStartMeasurer.addListener { coldStartInfo ->
            Logger.d(LOG_TAG, "Cold start reported: $coldStartInfo")
        }
//        AnrDetectors.create(
//            looper = application.mainLooper,
//            listener = OnAnrDetectedListener { looper, anrInfo ->
//                Logger.d(LOG_TAG, "Anr detected: info=${anrInfo.toStringDetailed()}")
//            }
//        ).start()

        if (BuildConfig.DEBUG) {
            AnrDetectors.create(
                looper = application.mainLooper,
                uiContextProvider = { ApplicationWatcher.foregroundActivity }
            )// .start()
        }
    }

    private fun setupRxPlugins() {
        RxJavaPlugins.setErrorHandler { err ->
            // Default error consumer
            eventLogger.log(err)
            runOnForegroundActivity { postError(err) }
        }
    }

    private fun setupFirebase() {
        if (BuildConfig.GOOGLE_SERVICES_ENABLED) {
            val instance = FirebaseRemoteConfig.getInstance()
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(TimeUnit.HOURS.toSeconds(24))
                .build()
            instance.setConfigSettingsAsync(configSettings)
            instance.setDefaultsAsync(R.xml.firebase_remote_config_default)
        }
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
        application.registerReceiver(receiver, intentFilter)
    }

    private fun setupMediaScanWork() {
        scheduleMediaScanWork(application)
    }

    private fun runOnForegroundActivity(action: BaseActivity.() -> Unit) {
        mainThreadHandler?.post {
            ThreadStrictMode.assertMain()
            (ApplicationWatcher.foregroundActivity as? BaseActivity)?.action()
        }
    }

    private fun noteLowMemory() {
        eventLogger.logLowMemory()
        Logger.w(LOG_TAG, "Low memory!")
        if (BuildInfo.isDebug()) {
            runOnForegroundActivity { postMessage("Low memory!") }
        }
    }

    private fun noteAppLaunch() {
        val totalLaunchCount = preferences.launchCount + 1 // +1 for the current launch
        preferences.setLaunchCount(totalLaunchCount)
        eventLogger.logAppLaunched(totalLaunchCount)
        Logger.d(LOG_TAG, "App launched for the $totalLaunchCount time")
    }

    companion object {
        private const val LOG_TAG = "AppStartUpInitializer"
    }
}