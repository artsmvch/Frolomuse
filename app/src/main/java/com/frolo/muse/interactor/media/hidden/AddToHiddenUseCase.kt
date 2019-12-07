package com.frolo.muse.interactor.media.hidden

import com.frolo.muse.model.media.MyFile
import com.frolo.muse.repository.MyFileRepository
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable
import javax.inject.Inject


class AddToHiddenUseCase @Inject constructor(
        private val repository: MyFileRepository,
        private val schedulerProvider: SchedulerProvider
) {

    fun addToHidden(item: MyFile): Completable {
        return repository.setFileHidden(item, true)
                .subscribeOn(schedulerProvider.worker())
    }

}