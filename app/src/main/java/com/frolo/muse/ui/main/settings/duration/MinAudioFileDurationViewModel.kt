package com.frolo.muse.ui.main.settings.duration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.arch.SingleLiveEvent
import com.frolo.muse.arch.call
import com.frolo.muse.engine.Player
import com.frolo.muse.interactor.media.get.removeShortAudioSources
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.logMinAudioFileDurationSet
import com.frolo.muse.repository.LibraryPreferences
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseViewModel
import javax.inject.Inject


class MinAudioFileDurationViewModel @Inject constructor(
    private val player: Player,
    private val libraryPreferences: LibraryPreferences,
    private val schedulerProvider: SchedulerProvider,
    private val eventLogger: EventLogger
): BaseViewModel(eventLogger) {

    private val currMinAudioDuration: LiveData<Long> by lazy {
        MutableLiveData<Long>().apply {
            libraryPreferences.getMinAudioDuration()
                .firstOrError()
                .observeOn(schedulerProvider.main())
                .subscribeFor { value = it }
        }
    }

    private val _minutes by lazy {
        MediatorLiveData<Long>().apply {
            addSource(currMinAudioDuration) { durationInSeconds ->
                if (durationInSeconds > 0) {
                    value = durationInSeconds / 60
                }
            }
        }
    }
    val minutes: LiveData<Long> get() = _minutes

    private val _seconds by lazy {
        MediatorLiveData<Long>().apply {
            addSource(currMinAudioDuration) { durationInSeconds ->
                if (durationInSeconds > 0) {
                    value = durationInSeconds % 60
                }
            }
        }
    }
    val seconds: LiveData<Long> get() = _seconds

    private val _goBackEvent = SingleLiveEvent<Unit>()
    val goBackEvent: LiveData<Unit> get() = _goBackEvent

    fun onCancelClicked() {
        _goBackEvent.call()
    }

    fun onSaveClicked(typedMinutes: Int, typedSeconds: Int) {
        val newDurationInSeconds = typedMinutes * 60L + typedSeconds
        val newDurationInMilliseconds = newDurationInSeconds * 1000L
        libraryPreferences.setMinAudioDuration(newDurationInMilliseconds)
            .doOnComplete { player.removeShortAudioSources(newDurationInSeconds) }
            .subscribeOn(schedulerProvider.worker())
            .observeOn(schedulerProvider.main())
            .doOnComplete { eventLogger.logMinAudioFileDurationSet(newDurationInSeconds) }
            .subscribeFor {
                _goBackEvent.call()
            }
    }

}