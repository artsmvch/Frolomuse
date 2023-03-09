package com.frolo.muse.ui.main.library

import android.app.Application
import androidx.appcompat.view.ContextThemeWrapper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.ads.AdListenerBuilder
import com.frolo.ads.AdMobUtils
import com.frolo.arch.support.distinctUntilChanged
import com.frolo.logger.api.Logger
import com.frolo.muse.interactor.ads.AdMobBannerUseCase2
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseAndroidViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import javax.inject.Inject


class LibraryViewModel @Inject constructor(
    application: Application,
    eventLogger: EventLogger,
    private val adMobBannerUseCase: AdMobBannerUseCase2,
    private val schedulerProvider: SchedulerProvider
): BaseAndroidViewModel(application, eventLogger) {

    private val _adView by lazy {
        MutableLiveData<AdView>(null).apply { loadAdAsync() }
    }
    val adView: LiveData<AdView> by lazy { _adView.distinctUntilChanged() }

    private fun loadAdAsync() {
        val startTime = System.currentTimeMillis()
        adMobBannerUseCase.getAdMobBannerConfig()
            .observeOn(schedulerProvider.main())
            .subscribeFor { bannerState ->
                val elapsedTime = System.currentTimeMillis() - startTime
                Logger.d(LOG_TAG, "Loaded Ad config in $elapsedTime millis")
                handleBannerState(bannerState)
            }
    }

    private fun handleBannerState(bannerState: AdMobBannerUseCase2.BannerState) {
        when (bannerState) {
            is AdMobBannerUseCase2.BannerState.Enabled -> {
                createAdMobBanner(bannerState.unitId)
            }
            AdMobBannerUseCase2.BannerState.Disabled -> {
                _adView.value = null
            }
        }
    }

    private fun createAdMobBanner(unitId: String) {
        val context = ContextThemeWrapper(justApplication,
            com.google.android.material.R.style.Theme_Material3_Dark)
        val adView = AdView(context)
        val adListener = AdListenerBuilder()
            .doWhenAdLoaded { _adView.value = adView }
            .doWhenAdFailedToLoad { _adView.value = null }
            .doLogging { msg -> Logger.d(LOG_TAG, msg) }
            .build()
        val adRequest = AdRequest.Builder()
            .build()
        adView.adListener = adListener
        adView.setAdSize(AdMobUtils.calculateSmartBannerAdSize(context))
        adView.adUnitId = unitId
        adView.loadAd(adRequest)
    }

    companion object {
        private const val LOG_TAG = "LibraryViewModel"
    }

}