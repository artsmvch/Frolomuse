package com.frolo.muse.interactor.media.hidden

import com.frolo.muse.model.media.MyFile
import com.frolo.muse.repository.MyFileRepository
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Flowable
import javax.inject.Inject


class GetHiddenFilesUseCase @Inject constructor(
        private val repository: MyFileRepository,
        private val schedulerProvider: SchedulerProvider
) {

    fun getHiddenFiles(): Flowable<List<MyFile>> {
        return repository.hiddenFiles
                .subscribeOn(schedulerProvider.worker())
    }

}