package com.frolo.muse.repository

import com.frolo.muse.model.ads.AdMobBannerConfig
import io.reactivex.Single


interface RemoteConfigRepository {
    fun isLyricsViewerEnabled(): Single<Boolean>
    fun isPlayerWakeLockEnabled(): Single<Boolean>
    fun isSnowfallFeatureEnabled(): Single<Boolean>
    fun isDonationFeatureEnabled(): Single<Boolean>
    fun getLibraryAdMobBannerConfig(): Single<AdMobBannerConfig>
}