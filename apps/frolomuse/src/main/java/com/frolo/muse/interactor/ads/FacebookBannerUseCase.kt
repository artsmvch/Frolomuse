package com.frolo.muse.interactor.ads

import android.content.Context
import com.frolo.muse.BuildConfig
import com.frolo.muse.BuildInfo
import com.frolo.muse.android.firstPackageInstallTime
import com.frolo.muse.model.ads.FacebookBannerConfig
import com.frolo.muse.repository.AppLaunchInfoProvider
import com.frolo.muse.repository.RemoteConfigRepository
import io.reactivex.Single
import javax.inject.Inject


class FacebookBannerUseCase @Inject constructor(
    private val context: Context,
    private val remoteConfigRepository: RemoteConfigRepository,
    private val appLaunchInfoProvider: AppLaunchInfoProvider,
) {
    fun getFacebookBannerState(): Single<BannerState> {
        return remoteConfigRepository.getFirebaseBannerConfig().map { config ->
            when {
                BuildInfo.isDebug() -> {
                    val testPlacementId = "IMG_16_9_APP_INSTALL#${config.placementId}"
                    BannerState.Enabled(testPlacementId)
                }
                shouldCreateAdMobBanner(config) -> {
                    BannerState.Enabled(config.placementId)
                }
                else -> BannerState.Disabled
            }
        }
    }

    private fun shouldCreateAdMobBanner(config: FacebookBannerConfig): Boolean {
        return config.isEnabled &&
                config.minAppVersionCode <= BuildConfig.VERSION_CODE &&
                config.minLaunchCount <= appLaunchInfoProvider.launchCount &&
                config.minFirstInstallTime <= context.firstPackageInstallTime
    }

    sealed class BannerState {
        data class Enabled(
            val placementId: String
        ): BannerState()

        object Disabled: BannerState()
    }
}