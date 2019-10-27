package com.frolo.muse.interactor.media.get

import com.frolo.muse.model.Library
import com.frolo.muse.model.media.Media
import com.frolo.muse.repository.MediaRepository
import com.frolo.muse.repository.Preferences
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Flowable


class GetAllMediaUseCase<E: Media> constructor(
        @Library.Section section: Int,
        schedulerProvider: SchedulerProvider,
        private val repository: MediaRepository<E>,
        preferences: Preferences
): GetSectionedMediaUseCase<E>(section, schedulerProvider, repository, preferences) {

    override fun getSortedCollection(sortOrder: String): Flowable<List<E>> {
        return repository.getAllItems(sortOrder)
    }

}