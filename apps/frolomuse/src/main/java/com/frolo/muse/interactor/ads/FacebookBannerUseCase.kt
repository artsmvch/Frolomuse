package com.frolo.muse.interactor.ads

import android.content.Context
import com.frolo.billing.BillingManager
import com.frolo.billing.PurchaseHistoryRecord
import com.frolo.billing.SkuType
import com.frolo.logger.api.Logger
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
    private val billingManager: BillingManager,
    private val remoteConfigRepository: RemoteConfigRepository,
    private val appLaunchInfoProvider: AppLaunchInfoProvider,
) {
    fun getFacebookBannerState(): Single<BannerState> {
        return Single
            .zip(hasNonEmptyPurchaseHistory(), remoteConfigRepository.getFirebaseBannerConfig()) { hasNonEmptyPurchaseHistory, config ->
                when {
                    BuildInfo.isDebug() -> {
                        val testPlacementId = "IMG_16_9_APP_INSTALL#${config.placementId}"
                        BannerState.Enabled(testPlacementId)
                    }
                    hasNonEmptyPurchaseHistory -> {
                        // Something has been purchased before, don't bother customers with ads
                        BannerState.Disabled
                    }
                    shouldCreateAdMobBanner(config) -> {
                        BannerState.Enabled(config.placementId)
                    }
                    else -> BannerState.Disabled
                }
            }
    }

    private fun hasNonEmptyPurchaseHistory(): Single<Boolean> {
        // Check for all Sku types
        val sources = SkuType.values().map { skuType ->
            billingManager.getPurchaseHistory(skuType)
        }
        return Single
            .zip(sources) { arr ->
                arr.flatMap { it as List<PurchaseHistoryRecord> }
            }
            .map { recordList ->
                recordList.any { it.quantity > 0 }
            }
            .onErrorReturn { err ->
                Logger.e(err)
                false
            }
            .onErrorReturnItem(false)
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