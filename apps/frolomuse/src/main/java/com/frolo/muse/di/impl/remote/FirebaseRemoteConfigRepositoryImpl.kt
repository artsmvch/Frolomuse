package com.frolo.muse.di.impl.remote

import com.frolo.muse.firebase.FirebaseRemoteConfigCache
import com.frolo.muse.firebase.FirebaseRemoteConfigUtil
import com.frolo.muse.repository.RemoteConfigRepository
import io.reactivex.Single


class FirebaseRemoteConfigRepositoryImpl : RemoteConfigRepository {

    override fun isLyricsViewerEnabled(): Single<Boolean> {
        return FirebaseRemoteConfigCache
            .getBool(FirebaseRemoteConfigUtil.LYRICS_VIEWER_ENABLED)
            .onErrorReturnItem(false)
    }

    override fun isPlayerWakeLockEnabled(): Single<Boolean> {
        return FirebaseRemoteConfigCache
            .getBool(FirebaseRemoteConfigUtil.PLAYER_WAKE_LOCK_FEATURE_ENABLED)
            .onErrorReturnItem(false)
    }

    override fun isSnowfallFeatureEnabled(): Single<Boolean> {
        return FirebaseRemoteConfigCache
            .getBool(FirebaseRemoteConfigUtil.SNOWFALL_FEATURE_ENABLED)
            .onErrorReturnItem(false)
    }

    override fun isDonationFeatureEnabled(): Single<Boolean> {
        return FirebaseRemoteConfigCache
            .getBool(FirebaseRemoteConfigUtil.DONATION_FEATURE_ENABLED)
            .onErrorReturnItem(false)
    }

}