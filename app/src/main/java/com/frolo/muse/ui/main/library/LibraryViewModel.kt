package com.frolo.muse.ui.main.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.admob.AdMobs
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.repository.Preferences
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseViewModel
import javax.inject.Inject


class LibraryViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val preferences: Preferences,
    private val eventLogger: EventLogger
): BaseViewModel(eventLogger) {

    val isAdMobEnabled: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false).apply {
            AdMobs.isAdMobEnabled(minimumFetchIntervalInSeconds = 0L) // immediately fetch the configs from the server
                .observeOn(schedulerProvider.main())
                .doOnSuccess { isEnabled -> value = isEnabled }
                .subscribeFor { /* np-op */ }
        }
    }

}