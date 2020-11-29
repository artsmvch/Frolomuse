package com.frolo.muse.admob

import com.google.android.gms.tasks.SuccessContinuation
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.reactivex.Single
import java.util.concurrent.Executors


/**
 * Helper class for wrapping methods in [FirebaseRemoteConfig] in Rx and  other useful stuff.
 */
object AdMobs {

    private const val KEY_AD_MOB_ENABLED = "ad_mob_enabled"

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

    fun isAdMobEnabled(minimumFetchIntervalInSeconds: Long? = null): Single<Boolean> {
        return fetchAndActivate(minimumFetchIntervalInSeconds)
            .map { activated ->
                // TODO: the activation result is always false, what we gon do?
                FirebaseRemoteConfig.getInstance()
                    .getValue(KEY_AD_MOB_ENABLED)
                    .let { configValue ->
                        configValue.asString() == "true"
                    }
            }
    }

}