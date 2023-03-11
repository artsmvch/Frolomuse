package com.frolo.muse.ui.main.library

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.arch.support.distinctUntilChanged
import com.frolo.logger.api.Logger
import com.frolo.muse.interactor.ads.AdMobBannerUseCase2
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseAndroidViewModel
import javax.inject.Inject


class LibraryViewModel @Inject constructor(
    application: Application,
    eventLogger: EventLogger,
    private val adMobBannerUseCase: AdMobBannerUseCase2,
    private val schedulerProvider: SchedulerProvider
): BaseAndroidViewModel(application, eventLogger) {

    private val _bannerConfig by lazy {
        MutableLiveData<BannerConfig>(null).apply(::loadAdConfigAsync)
    }
    val bannerConfig: LiveData<BannerConfig> by lazy { _bannerConfig.distinctUntilChanged() }

    private fun loadAdConfigAsync(liveData: MutableLiveData<BannerConfig>) {
        val startTime = System.currentTimeMillis()
        adMobBannerUseCase.getAdMobBannerConfig()
            .observeOn(schedulerProvider.main())
            .subscribeFor { bannerState ->
                val elapsedTime = System.currentTimeMillis() - startTime
                Logger.d(LOG_TAG, "Loaded Ad config in $elapsedTime millis")
                liveData.value = when (bannerState) {
                    is AdMobBannerUseCase2.BannerState.Enabled ->
                        BannerConfig(unitId = bannerState.unitId)
                    AdMobBannerUseCase2.BannerState.Disabled -> null
                }
            }
    }

    data class BannerConfig(
        val unitId: String
    )

    companion object {
        private const val LOG_TAG = "LibraryViewModel"
    }

}