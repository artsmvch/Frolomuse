package com.frolo.muse.interactor.ads

import android.content.Context
import com.frolo.muse.BuildConfig
import com.frolo.muse.admob.AdMobs
import com.frolo.muse.android.firstPackageInstallTime
import com.frolo.muse.repository.Preferences
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Single
import javax.inject.Inject

class ShowBannerUseCase @Inject constructor(
    private val context: Context,
    private val preferences: Preferences,
    private val schedulerProvider: SchedulerProvider
) {

    fun canShowBanner(): Single<Boolean> {
        if (DEBUG) {
            return Single.just(true)
        }

        // immediately fetch the configs from the server
        return AdMobs.getAdMobRemoteConfigs(minimumFetchIntervalInSeconds = 0)
            .observeOn(schedulerProvider.main())
            .map { config ->
                val firstPackageInstallTime = context.firstPackageInstallTime
                if (config.thresholdInstallTime != null && firstPackageInstallTime < config.thresholdInstallTime) {
                    return@map false
                }

                val openCount = preferences.openCount
                if (config.thresholdOpenCount != null && openCount < config.thresholdOpenCount) {
                    return@map false
                }

                return@map config.isEnabled
            }
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