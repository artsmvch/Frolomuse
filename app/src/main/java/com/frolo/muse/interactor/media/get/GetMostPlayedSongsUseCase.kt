package com.frolo.muse.interactor.media.get

import com.frolo.muse.model.media.SongWithPlayCount
import com.frolo.muse.repository.SongRepository
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Flowable


class GetMostPlayedSongsUseCase constructor(
        private val repository: SongRepository,
        private val schedulerProvider: SchedulerProvider
) {

    fun getMostPlayedSongsWithPlayCount(): Flowable<List<SongWithPlayCount>> {
        return repository.getSongsWithPlayCount(1)
                .map { list -> list.sortedBy { it.playCount } }
                .subscribeOn(schedulerProvider.worker())
    }

}