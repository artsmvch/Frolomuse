package com.frolo.muse.interactor.ads

import android.content.Context
import com.frolo.ads.AdMobUtils
import com.frolo.billing.BillingManager
import com.frolo.muse.BuildInfo
import com.frolo.muse.android.firstPackageInstallTime
import com.frolo.muse.billing.Products
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
            .zip(isAnyProductPurchased(), remoteConfigRepository.getMainAdMobBannerConfig()) { isPurchased, config ->
                when {
                    BuildInfo.isDebug() -> {
                        BannerState.Enabled(AdMobUtils.TEST_BANNER_ID)
                    }
                    isPurchased -> {
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

    private fun isAnyProductPurchased(): Single<Boolean> {
        val sources = listOf(
            Products.PREMIUM,
            Products.DONATE_THANKS,
            Products.DONATE_COFFEE,
            Products.DONATE_MOVIE_TICKET,
            Products.DONATE_PIZZA,
            Products.DONATE_MEAL,
            Products.DONATE_GYM_MEMBERSHIP
        ).map { productId ->
            billingManager.isProductPurchased(productId).first(false)
        }
        return Single
            .zip(sources) { values -> values.any { it == true } }
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