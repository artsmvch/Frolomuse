package com.frolo.muse.interactor.media

import com.frolo.muse.model.media.Media
import com.frolo.muse.repository.MediaRepository
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Single


class ChangeFavouriteUseCase <E: Media> constructor(
        private val schedulerProvider: SchedulerProvider,
        private val repository: MediaRepository<E>
) {

    fun getIsFavourite(item: E): Single<Boolean> {
        return repository.isFavourite(item)
                .subscribeOn(schedulerProvider.worker())
    }

    fun changeFavourite(item: E): Single<Boolean> {
        return repository.changeFavourite(item)
                .subscribeOn(schedulerProvider.worker())

    }

}