package com.frolo.muse.ui.main.audiofx.preset

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.arch.support.SingleLiveEvent
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.logCustomPresetSaved
import com.frolo.audiofx.CustomPreset
import com.frolo.muse.repository.PresetRepository
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseViewModel


class SavePresetViewModel constructor(
        private val schedulerProvider: SchedulerProvider,
        private val repository: PresetRepository,
        private val eventLogger: EventLogger,
        private val bandLevelsArg: ShortArray
): BaseViewModel(eventLogger) {

    private val _isSavingPreset: MutableLiveData<Boolean> = SingleLiveEvent()
    val isSavingPreset: LiveData<Boolean> = _isSavingPreset

    private val _savingError: MutableLiveData<Throwable> = MutableLiveData()
    val savingError: LiveData<Throwable> = _savingError

    private val _presetSavedEvent: MutableLiveData<CustomPreset> = SingleLiveEvent()
    val presetSavedEvent: LiveData<CustomPreset> = _presetSavedEvent

    fun onSaveButtonClicked(typedName: String) {
        repository.create(typedName, bandLevelsArg)
                .subscribeOn(schedulerProvider.worker())
                .observeOn(schedulerProvider.main())
                .doOnSubscribe { _isSavingPreset.value = true }
                .doFinally { _isSavingPreset.value = false }
                .doOnSuccess { eventLogger.logCustomPresetSaved() }
                .subscribe { preset, err ->
                    if (preset != null) {
                        _presetSavedEvent.value = preset
                    } else if (err != null) {
                        _savingError.value = err
                    }
                }
                .save()
    }

}