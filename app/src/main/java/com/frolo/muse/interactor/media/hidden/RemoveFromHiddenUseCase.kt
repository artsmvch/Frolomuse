package com.frolo.muse.interactor.media.hidden

import com.frolo.music.model.MyFile
import com.frolo.music.repository.MyFileRepository
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable
import javax.inject.Inject


class RemoveFromHiddenUseCase @Inject constructor(
    private val repository: MyFileRepository,
    private val schedulerProvider: SchedulerProvider
) {

    fun removeFromHidden(item: MyFile): Completable {
        return repository.setFileHidden(item, false)
            .subscribeOn(schedulerProvider.worker())
    }

}