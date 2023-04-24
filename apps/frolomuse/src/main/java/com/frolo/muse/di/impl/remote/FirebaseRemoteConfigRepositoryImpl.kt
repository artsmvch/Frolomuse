package com.frolo.muse.di.impl.remote

import android.content.Context
import com.frolo.muse.R
import com.frolo.muse.firebase.FirebaseRemoteConfigCache
import com.frolo.muse.firebase.FirebaseRemoteConfigUtil
import com.frolo.muse.model.ads.FacebookBannerConfig
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

    override fun getFirebaseBannerConfig(): Single<FacebookBannerConfig> {
        val defaultConfig = FacebookBannerConfig(
            isEnabled = true,
            placementId = "", //context.getString(R.string.facebook_ads_placement_id),
            minAppVersionCode = 158,
            minFirstInstallTime = 1678554000, // Sat Mar 11 2023 17:00:00 GMT+0000
            minLaunchCount = 10
        )
        return FirebaseRemoteConfigCache
            .getString(FirebaseRemoteConfigUtil.LIBRARY_ADMOB_BANNER_CONFIG)
            .map { rawConfig ->
                val configJson = JSONObject(rawConfig)
                FacebookBannerConfig(
                    isEnabled = configJson.optBoolean("is_enabled", defaultConfig.isEnabled),
                    placementId = configJson.optString("placement_id", defaultConfig.placementId),
                    minAppVersionCode = configJson.optLong("min_app_version_code", defaultConfig.minAppVersionCode),
                    minFirstInstallTime = configJson.optLong("min_first_install_time", defaultConfig.minFirstInstallTime),
                    minLaunchCount = configJson.optInt("min_launch_count", defaultConfig.minLaunchCount)
                )
            }
            .onErrorReturnItem(defaultConfig)
    }

}