package com.frolo.muse.ui.main.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.admob.BannerState
import com.frolo.muse.interactor.ads.AdMobBannerUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.logBannerCanBeShown
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseViewModel
import javax.inject.Inject


class LibraryViewModel @Inject constructor(
    private val adMobBannerUseCase: AdMobBannerUseCase,
    private val schedulerProvider: SchedulerProvider,
    private val eventLogger: EventLogger
): BaseViewModel(eventLogger) {

    val bannerState: LiveData<BannerState> by lazy {
        MutableLiveData<BannerState>().apply {
            adMobBannerUseCase.getLibraryBannerState()
                .observeOn(schedulerProvider.main())
                .subscribeFor { state ->
                    value = state
                    if (state.canBeShown) {
                        eventLogger.logBannerCanBeShown()
                    }
                }
        }
    }

}