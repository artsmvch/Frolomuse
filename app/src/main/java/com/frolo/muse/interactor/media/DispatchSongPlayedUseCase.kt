package com.frolo.muse.interactor.media

import com.frolo.muse.model.media.Song
import com.frolo.muse.repository.SongRepository
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable


class DispatchSongPlayedUseCase constructor(
        private val schedulerProvider: SchedulerProvider,
        private val songRepository: SongRepository
) {

    fun dispatchSongPlayed(song: Song): Completable {
        return songRepository.addSongPlayCount(song, 1)
                .subscribeOn(schedulerProvider.worker())
    }

}