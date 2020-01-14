package com.frolo.muse.interactor.media.favourite

import com.frolo.muse.model.media.Media
import com.frolo.muse.repository.MediaRepository
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Flowable
import javax.inject.Inject


class GetIsFavouriteUseCase<T: Media> @Inject constructor(
        private val repository: MediaRepository<T>,
        private val schedulerProvider: SchedulerProvider
) {

    fun isFavourite(item: T): Flowable<Boolean> =
            repository.isFavourite(item)
                    .subscribeOn(schedulerProvider.worker())

}