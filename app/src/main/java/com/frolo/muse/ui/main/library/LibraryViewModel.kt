package com.frolo.muse.ui.main.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.interactor.ads.ShowBannerUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.logBannerShown
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseViewModel
import javax.inject.Inject


class LibraryViewModel @Inject constructor(
    private val showBannerUseCase: ShowBannerUseCase,
    private val schedulerProvider: SchedulerProvider,
    private val eventLogger: EventLogger
): BaseViewModel(eventLogger) {

    val canShowBanner: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false).apply {
            showBannerUseCase.canShowBanner()
                .observeOn(schedulerProvider.main())
                .subscribeFor { canShow ->
                    value = canShow
                    if (canShow) {
                        eventLogger.logBannerShown()
                    }
                }
        }
    }

}