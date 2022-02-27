package com.frolo.muse.interactor.media.favourite

import com.frolo.music.model.Song
import com.frolo.music.repository.SongRepository
import io.reactivex.Completable
import javax.inject.Inject


class ChangeSongFavStatusUseCase @Inject constructor(
    private val songRepository: SongRepository
) {
    fun changeSongFavStatus(song: Song): Completable = songRepository.changeFavourite(song)
}