package com.frolo.muse.admob

import com.google.android.gms.tasks.SuccessContinuation
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import io.reactivex.Single
import java.util.concurrent.Executors


/**
 * Helper class for wrapping methods in [FirebaseRemoteConfig] in Rx and  other useful stuff.
 */
object AdMobs {

    private const val KEY_AD_MOB_INIT_ON_COLD_START = "ad_mob_init_on_cold_start"
    private const val KEY_AD_MOB_ENABLED = "ad_mob_enabled"
    private const val KEY_AD_MOB_THRESHOLD_INSTALL_TIME = "ad_mob_threshold_install_time"
    private const val KEY_AD_MOB_THRESHOLD_OPEN_COUNT = "ad_mob_threshold_open_count"

    private const val INIT_ON_COLD_START_DEFAULT = true

    private val executor = Executors.newFixedThreadPool(2)

    fun fetchAndActivate(minimumFetchIntervalInSeconds: Long? = null): Single<Boolean> {
        return Single.create { emitter ->
            val configInstance = FirebaseRemoteConfig.getInstance()
            val task = if (minimumFetchIntervalInSeconds != null && minimumFetchIntervalInSeconds >= 0L) {
                val continuation = SuccessContinuation<Void, Boolean> { configInstance.activate() }
                configInstance.fetch(minimumFetchIntervalInSeconds).onSuccessTask(executor, continuation)
            } else {
                configInstance.fetchAndActivate()
            }

            task.addOnFailureListener {
                if (!emitter.isDisposed) {
                    emitter.onError(it)
                }
            }

            task.addOnCompleteListener { _task ->
                if (!emitter.isDisposed) {
                    if (_task.isSuccessful) {
                        emitter.onSuccess(_task.result)
                    } else {
                        val err: Exception = _task.exception ?: IllegalStateException("Task is not successful but the exception is null")
                        emitter.onError(err)
                    }
                }
            }
        }
    }

    @Deprecated("Use getAdMobRemoteConfigs method")
    fun isAdMobEnabled(minimumFetchIntervalInSeconds: Long? = null): Single<Boolean> {
        return getAdMobRemoteConfigs(minimumFetchIntervalInSeconds).map { it.isEnabled }
    }

    fun getAdMobRemoteConfigs(minimumFetchIntervalInSeconds: Long? = null): Single<AdMobRemoteConfigs> {
        return fetchAndActivate(minimumFetchIntervalInSeconds)
            .map { activated ->
                // TODO: the activation result is always false, what we gon do?

                val remoteConfigInstance = FirebaseRemoteConfig.getInstance()

                val isEnabled = remoteConfigInstance[KEY_AD_MOB_ENABLED].asString() == "true"
                val thresholdInstallTime = remoteConfigInstance[KEY_AD_MOB_THRESHOLD_INSTALL_TIME].asString().let { stringValue ->
                    try {
                        stringValue.toLong()
                    } catch (ignored: Throwable) {
                        null
                    }
                }
                val thresholdOpenCount = remoteConfigInstance[KEY_AD_MOB_THRESHOLD_OPEN_COUNT].asString().let { stringValue ->
                    try {
                        stringValue.toInt()
                    } catch (ignored: Throwable) {
                        null
                    }
                }

                AdMobRemoteConfigs(
                    isEnabled = isEnabled,
                    thresholdInstallTime = thresholdInstallTime,
                    thresholdOpenCount = thresholdOpenCount
                )
            }
    }

    fun shouldInitializeOnColdStart(): Boolean {
        return try {
            val remoteConfigInstance = FirebaseRemoteConfig.getInstance()
            remoteConfigInstance.getBoolean(KEY_AD_MOB_INIT_ON_COLD_START)
        } catch (ignored: Throwable) {
            INIT_ON_COLD_START_DEFAULT
        }
    }

    data class AdMobRemoteConfigs(
        val isEnabled: Boolean,
        val thresholdInstallTime: Long?,
        val thresholdOpenCount: Int?
    )

}