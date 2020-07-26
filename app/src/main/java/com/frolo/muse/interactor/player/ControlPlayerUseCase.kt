package com.frolo.muse.interactor.player

import com.frolo.muse.common.albumId
import com.frolo.muse.common.artistId
import com.frolo.muse.engine.Player
import com.frolo.muse.model.media.Album
import com.frolo.muse.model.media.Artist
import com.frolo.muse.model.media.Genre
import com.frolo.muse.repository.AlbumRepository
import com.frolo.muse.repository.ArtistRepository
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Single
import javax.inject.Inject


class ControlPlayerUseCase @Inject constructor(
        private val player: Player,
        private val schedulerProvider: SchedulerProvider,
        private val albumRepository: AlbumRepository,
        private val artistRepository: ArtistRepository
) {

    fun getAlbum(): Single<Album> {
        return Single.fromCallable { player.getCurrent()?.albumId }
                .flatMap { id -> albumRepository.getItem(id).firstOrError() }
                .subscribeOn(schedulerProvider.worker())
    }

    fun getArtist(): Single<Artist> {
        return Single.fromCallable { player.getCurrent()?.artistId }
                .flatMap { id -> artistRepository.getItem(id).firstOrError() }
                .subscribeOn(schedulerProvider.worker())
    }

    fun getGenre(): Single<Genre> {
        return Single.error(UnsupportedOperationException())
    }

}