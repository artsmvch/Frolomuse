package com.frolo.muse.interactor.ads

import android.content.Context
import com.frolo.ads.AdMobUtils
import com.frolo.billing.BillingManager
import com.frolo.billing.PurchaseHistoryRecord
import com.frolo.billing.SkuType
import com.frolo.logger.api.Logger
import com.frolo.muse.BuildInfo
import com.frolo.muse.android.firstPackageInstallTime
import com.frolo.muse.model.ads.AdMobBannerConfig
import com.frolo.muse.repository.AppLaunchInfoProvider
import com.frolo.muse.repository.RemoteConfigRepository
import io.reactivex.Single
import javax.inject.Inject


class AdMobBannerUseCase2 @Inject constructor(
    private val context: Context,
    private val billingManager: BillingManager,
    private val remoteConfigRepository: RemoteConfigRepository,
    private val appLaunchInfoProvider: AppLaunchInfoProvider,
) {
    fun getAdMobBannerConfig(): Single<BannerState> {
        return Single
            .zip(hasNonEmptyPurchaseHistory(), remoteConfigRepository.getMainAdMobBannerConfig()) { hasNonEmptyPurchaseHistory, config ->
                when {
                    BuildInfo.isDebug() -> {
                        BannerState.Enabled(AdMobUtils.TEST_BANNER_ID)
                    }
                    hasNonEmptyPurchaseHistory -> {
                        // Something has been purchased before, don't bother customers with ads
                        BannerState.Disabled
                    }
                    shouldCreateAdMobBanner(config) -> {
                        BannerState.Enabled(config.unitId)
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

    private fun shouldCreateAdMobBanner(config: AdMobBannerConfig): Boolean {
        return config.isEnabled &&
                config.minLaunchCount <= appLaunchInfoProvider.launchCount &&
                config.minFirstInstallTime <= context.firstPackageInstallTime
    }

    sealed class BannerState {
        data class Enabled(
            val unitId: String
        ): BannerState()

        object Disabled: BannerState()
    }
}