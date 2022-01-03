package com.frolo.muse.repository

import io.reactivex.Single


interface RemoteConfigRepository {
    fun isLyricsViewerEnabled(): Single<Boolean>
    fun isPurchaseFeatureEnabled(): Single<Boolean>
    fun isPlayerWakeLockEnabled(): Single<Boolean>
    fun isSnowfallFeatureEnabled(): Single<Boolean>
    fun isDonationFeatureEnabled(): Single<Boolean>
}