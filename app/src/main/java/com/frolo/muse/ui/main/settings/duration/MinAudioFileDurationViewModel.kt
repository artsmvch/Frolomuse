package com.frolo.muse.ui.main.settings.duration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.arch.SingleLiveEvent
import com.frolo.muse.arch.call
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.logMinAudioFileDurationSet
import com.frolo.muse.repository.Preferences
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseViewModel
import javax.inject.Inject


class MinAudioFileDurationViewModel @Inject constructor(
    private val preferences: Preferences,
    private val schedulerProvider: SchedulerProvider,
    private val eventLogger: EventLogger
): BaseViewModel(eventLogger) {

    private val currMinAudioDuration: LiveData<Int> by lazy {
        MutableLiveData<Int>().apply {
            preferences.minAudioFileDuration
                .firstOrError()
                .observeOn(schedulerProvider.main())
                .subscribeFor { value = it }
        }
    }

    private val _minutes by lazy {
        MediatorLiveData<Int>().apply {
            addSource(currMinAudioDuration) { durationInSeconds ->
                if (durationInSeconds > 0) {
                    value = durationInSeconds / 60
                }
            }
        }
    }
    val minutes: LiveData<Int> get() = _minutes

    private val _seconds by lazy {
        MediatorLiveData<Int>().apply {
            addSource(currMinAudioDuration) { durationInSeconds ->
                if (durationInSeconds > 0) {
                    value = durationInSeconds % 60
                }
            }
        }
    }
    val seconds: LiveData<Int> get() = _seconds

    private val _goBackEvent = SingleLiveEvent<Unit>()
    val goBackEvent: LiveData<Unit> get() = _goBackEvent

    fun onCancelClicked() {
        _goBackEvent.call()
    }

    fun onSaveClicked(typedMinutes: Int, typedSeconds: Int) {
        val newDuration = typedMinutes * 60 + typedSeconds
        preferences.setMinAudioFileDuration(newDuration)
            .doOnComplete { eventLogger.logMinAudioFileDurationSet(newDuration) }
            .subscribeFor(schedulerProvider){
                _goBackEvent.call()
            }
    }

}