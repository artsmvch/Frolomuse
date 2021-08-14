package com.frolo.muse.firebase

import com.google.android.gms.tasks.SuccessContinuation
import com.google.android.gms.tasks.Task
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.reactivex.Single
import java.util.concurrent.Executors


object FirebaseRemoteConfigUtil {

    private const val STRICT_ACTIVATION = false

    const val LYRICS_VIEWER_ENABLED = "lyrics_viewer_enabled"
    const val PURCHASE_FEATURE_ENABLED = "purchase_feature_enabled"
    const val PLAYER_WAKE_LOCK_FEATURE_ENABLED = "player_wake_lock_feature_enabled"

    private val executor = Executors.newFixedThreadPool(2)

    private fun getActivatedConfig(fetch: Boolean, minimumFetchIntervalInSeconds: Long? = null): Single<FirebaseRemoteConfig> {
        return Single.create { emitter ->
            val configInstance = FirebaseRemoteConfig.getInstance()

            val task: Task<Boolean> = if (fetch) {
                if (minimumFetchIntervalInSeconds != null && minimumFetchIntervalInSeconds >= 0L) {
                    val continuation = SuccessContinuation<Void, Boolean> { configInstance.activate() }
                    configInstance.fetch(minimumFetchIntervalInSeconds).onSuccessTask(executor, continuation)
                } else {
                    configInstance.fetchAndActivate()
                }
            } else {
                configInstance.activate()
            }

            task.addOnFailureListener {
                if (!emitter.isDisposed) {
                    emitter.onError(it)
                }
            }

            task.addOnCompleteListener { _task ->
                if (!emitter.isDisposed) {
                    if (_task.isSuccessful) {
                        val isActivated = _task.result
                        if (!STRICT_ACTIVATION || isActivated) {
                            emitter.onSuccess(FirebaseRemoteConfig.getInstance())
                        } else {
                            val err: Exception = IllegalStateException("Failed to activate Firebase config instance")
                            emitter.onError(err)
                        }
                    } else {
                        val err: Exception = _task.exception ?: IllegalStateException("Task is not successful but the exception is null")
                        emitter.onError(err)
                    }
                }
            }
        }
    }

    fun fetchAndActivate(minimumFetchIntervalInSeconds: Long? = null): Single<FirebaseRemoteConfig> {
        return getActivatedConfig(fetch = true, minimumFetchIntervalInSeconds = minimumFetchIntervalInSeconds)
    }

    fun getActivatedConfig(): Single<FirebaseRemoteConfig> {
        return getActivatedConfig(fetch = false)
    }

}