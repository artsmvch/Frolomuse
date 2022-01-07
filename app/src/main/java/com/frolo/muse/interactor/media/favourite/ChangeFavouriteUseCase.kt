package com.frolo.muse.interactor.media.favourite

import com.frolo.music.model.Media
import com.frolo.music.repository.MediaRepository
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable


class ChangeFavouriteUseCase <E: Media> constructor(
        private val schedulerProvider: SchedulerProvider,
        private val repository: MediaRepository<E>
) {

    fun changeFavourite(item: E): Completable =
            repository.changeFavourite(item)
                    .subscribeOn(schedulerProvider.worker())

}