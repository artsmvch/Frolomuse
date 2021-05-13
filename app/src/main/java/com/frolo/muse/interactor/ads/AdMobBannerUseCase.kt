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
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Flowable
import io.reactivex.functions.BiFunction
import javax.inject.Inject


class AdMobBannerUseCase @Inject constructor(
    private val context: Context,
    private val billingManager: BillingManager,
    private val preferences: Preferences,
    private val schedulerProvider: SchedulerProvider
) {

    // The default library banner id, hardcoded in the app code.
    private fun getDefaultLibraryBannerId(): String {
        return context.getString(R.string.ad_mob_library_unit_id)
    }

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
                        getDefaultLibraryBannerId()
                    }
                    BannerState(
                        canBeShown = canBeShown(config),
                        bannerId = bannerId
                    )
                }
                .toFlowable()
        }

        val isPremiumPurchasedSource: Flowable<Boolean> =
                billingManager.isProductPurchased(productId = ProductId.PREMIUM, forceCheckFromApi = true)

        val sourceCombiner = BiFunction<BannerState, Boolean, BannerState> { state, isPremiumPurchased ->
            when {
                isPremiumPurchased -> {
                    // The user has purchased the premium, so he is completely free from advertising
                    state.copy(canBeShown = false)
                }
                else -> {
                    // It goes as it is.
                    state
                }
            }
        }

        return Flowable.combineLatest(remoteConfigsSource, isPremiumPurchasedSource, sourceCombiner)
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