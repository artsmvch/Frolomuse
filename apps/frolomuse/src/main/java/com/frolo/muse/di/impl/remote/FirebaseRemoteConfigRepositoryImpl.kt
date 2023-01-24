package com.frolo.muse.di.impl.remote

import android.content.Context
import com.frolo.muse.R
import com.frolo.muse.firebase.FirebaseRemoteConfigCache
import com.frolo.muse.firebase.FirebaseRemoteConfigUtil
import com.frolo.muse.model.ads.AdMobBannerConfig
import com.frolo.muse.repository.RemoteConfigRepository
import io.reactivex.Single
import org.json.JSONObject


class FirebaseRemoteConfigRepositoryImpl(
    private val context: Context
) : RemoteConfigRepository {

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

    override fun getMainAdMobBannerConfig(): Single<AdMobBannerConfig> {
        val defaultConfig = AdMobBannerConfig(
            isEnabled = true,
            unitId = context.getString(R.string.admob_ad_unit_id_main_screen),
            minFirstInstallTime = 1672873200, // January 5, 2023 11:00:00 AM
            minLaunchCount = 10
        )
        return FirebaseRemoteConfigCache
            .getString(FirebaseRemoteConfigUtil.MAIN_ADMOB_BANNER_CONFIG)
            .map { rawConfig ->
                val configJson = JSONObject(rawConfig)
                AdMobBannerConfig(
                    isEnabled = configJson.optBoolean("is_enabled", defaultConfig.isEnabled),
                    unitId = configJson.optString("unit_id", defaultConfig.unitId),
                    minFirstInstallTime = configJson.optLong("min_first_install_time", defaultConfig.minFirstInstallTime),
                    minLaunchCount = configJson.optInt("min_launch_count", defaultConfig.minLaunchCount)
                )
            }
            .onErrorReturnItem(defaultConfig)
    }

}