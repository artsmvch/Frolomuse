package com.frolo.muse.di.impl.remote

import com.frolo.muse.firebase.FirebaseRemoteConfigUtil
import com.frolo.muse.repository.RemoteConfigRepository
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue
import com.google.firebase.remoteconfig.ktx.get
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers


class FirebaseRemoteConfigRepositoryImpl : RemoteConfigRepository {

    private fun <V> getActivatedConfigValue(key: String, valueMapper: (FirebaseRemoteConfigValue) -> V): Single<V> {
        return FirebaseRemoteConfigUtil.getActivatedConfig()
            .map { config ->
                config[key].let { value ->
                    valueMapper.invoke(value)
                }
            }
    }

    override fun isLyricsViewerEnabled(): Single<Boolean> {
        return getActivatedConfigValue(FirebaseRemoteConfigUtil.LYRICS_VIEWER_ENABLED) { value ->
            try {
                value.asString() == "true"
            } catch (ignored: Throwable) {
                false
            }
        }
    }

    override fun isPurchaseFeatureEnabled(): Single<Boolean> {
        return getActivatedConfigValue(FirebaseRemoteConfigUtil.PURCHASE_FEATURE_ENABLED) { value ->
            try {
                value.asString() == "true"
            } catch (ignored: Throwable) {
                false
            }
        }
    }

    override fun isPlayerWakeLockEnabled(): Single<Boolean> {
        return Single.fromCallable { FirebaseRemoteConfig.getInstance() }
            .map { config ->
                config[FirebaseRemoteConfigUtil.PLAYER_WAKE_LOCK_FEATURE_ENABLED].asString() == "true"
            }
            .subscribeOn(Schedulers.computation())
    }

}