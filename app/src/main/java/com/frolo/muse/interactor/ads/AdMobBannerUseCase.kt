package com.frolo.muse.interactor.ads

import android.content.Context
import androidx.annotation.MainThread
import com.frolo.muse.BuildConfig
import com.frolo.muse.R
import com.frolo.muse.admob.AdMobs
import com.frolo.muse.admob.BannerState
import com.frolo.muse.android.firstPackageInstallTime
import com.frolo.muse.billing.BillingManager
import com.frolo.muse.billing.ProductId
import com.frolo.muse.repository.Preferences
import com.frolo.muse.repository.RemoteConfigRepository
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Flowable
import io.reactivex.functions.Function3
import javax.inject.Inject


class AdMobBannerUseCase @Inject constructor(
    private val context: Context,
    private val billingManager: BillingManager,
    private val remoteConfigRepository: RemoteConfigRepository,
    private val preferences: Preferences,
    private val schedulerProvider: SchedulerProvider
) {

    fun getLibraryBannerState(): Flowable<BannerState> {
        val remoteConfigsSource: Flowable<BannerState>
        if (DEBUG) {
            // For debugging purpose, we always show the banner
            val state = BannerState(
                canBeShown = true,
                bannerId = context.getString(R.string.ad_mob_test_unit_id)
            )
            remoteConfigsSource = Flowable.just(state)
        } else {
            // Immediately fetch the configs from the server
            remoteConfigsSource = AdMobs.getAdMobRemoteConfigs(minimumFetchIntervalInSeconds = 0)
                .observeOn(schedulerProvider.main())
                .map { config ->
                    val bannerId = if (!config.libraryBannerId.isNullOrBlank()) {
                        config.libraryBannerId
                    } else {
                        context.getString(R.string.ad_mob_library_unit_id)
                    }
                    BannerState(
                        canBeShown = canBeShown(config),
                        bannerId = bannerId
                    )
                }
                .toFlowable()
        }

        val isPurchaseFeatureEnabledSource: Flowable<Boolean> =
                remoteConfigRepository.isPurchaseFeatureEnabled().toFlowable()

        val isPremiumPurchasedSource: Flowable<Boolean> =
                billingManager.isProductPurchased(productId = ProductId.PREMIUM, forceCheckFromApi = true)

        val sourceCombiner = Function3<BannerState, Boolean, Boolean, BannerState> { state, isPurchaseFeatureEnabled, isPremiumPurchased ->
            when {
                isPremiumPurchased -> {
                    // The user has purchased the premium, so he is completely free from advertising
                    state.copy(canBeShown = false)
                }
                isPurchaseFeatureEnabled -> {
                    // The purchase feature is enabled, but the user has not purchased the premium yet.
                    // We need to show the banner so that he wants to make the premium purchase.
                    state.copy(canBeShown = true)
                }
                else -> {
                    // It goes as it is.
                    state
                }
            }
        }

        return Flowable.combineLatest(remoteConfigsSource, isPurchaseFeatureEnabledSource, isPremiumPurchasedSource, sourceCombiner)
                .observeOn(schedulerProvider.main())
    }

    @MainThread
    private fun canBeShown(config: AdMobs.AdMobRemoteConfigs): Boolean {
        val firstPackageInstallTime = context.firstPackageInstallTime
        if (config.thresholdInstallTime != null && firstPackageInstallTime < config.thresholdInstallTime) {
            return false
        }

        val openCount = preferences.openCount
        if (config.thresholdOpenCount != null && openCount < config.thresholdOpenCount) {
            return false
        }

        return config.isEnabled
    }

    companion object {
        private val DEBUG = BuildConfig.DEBUG

        @Deprecated("This is controlled from the remote configs")
        private const val INSTALL_TIME_THRESHOLD_FOR_AD = 1609459200L // 2020.01.01 00:00:00

        // The min open count to show ads to the user
        @Deprecated("This is controlled from the remote configs")
        private const val OPEN_COUNT_THRESHOLD_FOR_AD = 10

        private fun now(): Long = System.currentTimeMillis()
    }

}