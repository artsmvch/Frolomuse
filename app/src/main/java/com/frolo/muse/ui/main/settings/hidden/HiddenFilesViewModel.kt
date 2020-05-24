package com.frolo.muse.ui.main.settings.hidden

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.arch.combine
import com.frolo.muse.interactor.media.hidden.GetHiddenFilesUseCase
import com.frolo.muse.interactor.media.hidden.RemoveFromHiddenUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.logFilesUnhidden
import com.frolo.muse.model.media.MyFile
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseViewModel
import javax.inject.Inject


class HiddenFilesViewModel @Inject constructor(
        private val getHiddenFilesUseCase: GetHiddenFilesUseCase,
        private val removeFromHiddenUseCase: RemoveFromHiddenUseCase,
        private val schedulerProvider: SchedulerProvider,
        private val eventLogger: EventLogger
): BaseViewModel(eventLogger) {

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _hiddenFiles by lazy {
        MutableLiveData<List<MyFile>>().apply {
            getHiddenFilesUseCase.getHiddenFiles()
                    .observeOn(schedulerProvider.main())
                    .doOnSubscribe { _isLoading.value = true }
                    .doOnNext { _isLoading.value = false }
                    .subscribeFor { value = it }
        }
    }
    val hiddenFiles: LiveData<List<MyFile>> get() = _hiddenFiles

    val placeholderVisible: LiveData<Boolean> = combine(
            hiddenFiles,
            isLoading
    ) { list: List<*>?, isLoading: Boolean? ->
        list.isNullOrEmpty() && isLoading != true
    }

    fun onRemoveClick(item: MyFile) {
        removeFromHiddenUseCase.removeFromHidden(item)
                .observeOn(schedulerProvider.main())
                .doOnComplete { eventLogger.logFilesUnhidden(fileCount = 1) }
                .subscribeFor {
                }
    }

}