package com.frolo.muse.interactor.media.hidden

import com.frolo.muse.model.media.MyFile
import com.frolo.muse.repository.MyFileRepository
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable
import javax.inject.Inject


class HideFilesUseCase @Inject constructor(
        private val repository: MyFileRepository,
        private val schedulerProvider: SchedulerProvider
) {

    fun hide(item: MyFile): Completable {
        return repository.setFileHidden(item, true)
                .subscribeOn(schedulerProvider.worker())
    }

    fun hide(items: Collection<MyFile>): Completable {
        val sources = items.map { repository.setFileHidden(it, true) }
        return Completable.merge(sources)
                .subscribeOn(schedulerProvider.worker())
    }

}