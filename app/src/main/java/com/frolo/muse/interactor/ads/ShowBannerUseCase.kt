package com.frolo.muse.interactor.ads

import android.content.Context
import com.frolo.muse.BuildConfig
import com.frolo.muse.admob.AdMobs
import com.frolo.muse.android.firstPackageInstallTime
import com.frolo.muse.repository.Preferences
import io.reactivex.Single
import javax.inject.Inject

class ShowBannerUseCase @Inject constructor(
    private val context: Context,
    private val preferences: Preferences
) {

    fun canShowBanner(): Single<Boolean> {
        if (DEBUG) {
            return Single.just(true)
        }

        if (now() < INSTALL_TIME_THRESHOLD_FOR_AD) {
            return Single.just(false)
        }

        val firstPackageInstallTime = context.firstPackageInstallTime
        if (firstPackageInstallTime < INSTALL_TIME_THRESHOLD_FOR_AD) {
            return Single.just(false)
        }

        if (preferences.openCount < OPEN_COUNT_THRESHOLD_FOR_AD) {
            return Single.just(false)
        }

        // immediately fetch the configs from the server
        return AdMobs.isAdMobEnabled(minimumFetchIntervalInSeconds = 0)
    }

    companion object {
        private val DEBUG = BuildConfig.DEBUG

        private const val INSTALL_TIME_THRESHOLD_FOR_AD = 1609459200L // 2020.01.01 00:00:00

        // The min open count to show ads to the user
        private const val OPEN_COUNT_THRESHOLD_FOR_AD = 10

        private fun now(): Long = System.currentTimeMillis()
    }

}