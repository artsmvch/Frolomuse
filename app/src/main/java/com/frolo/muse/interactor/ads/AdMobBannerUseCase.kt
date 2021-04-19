package com.frolo.muse.interactor.ads

import android.content.Context
import androidx.annotation.MainThread
import com.frolo.muse.BuildConfig
import com.frolo.muse.R
import com.frolo.muse.admob.AdMobs
import com.frolo.muse.admob.BannerState
import com.frolo.muse.android.firstPackageInstallTime
import com.frolo.muse.repository.Preferences
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Single
import javax.inject.Inject


class AdMobBannerUseCase @Inject constructor(
    private val context: Context,
    private val preferences: Preferences,
    private val schedulerProvider: SchedulerProvider
) {

    fun getLibraryBannerState(): Single<BannerState> {
        if (DEBUG) {
            val state = BannerState(
                canBeShown = true,
                bannerId = context.getString(R.string.ad_mob_test_unit_id)
            )
            return Single.just(state)
        }

        // Immediately fetch the configs from the server
        return AdMobs.getAdMobRemoteConfigs(minimumFetchIntervalInSeconds = 0)
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