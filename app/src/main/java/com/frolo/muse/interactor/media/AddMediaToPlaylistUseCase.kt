package com.frolo.muse.interactor.media

import com.frolo.muse.model.media.Media
import com.frolo.muse.model.media.Playlist
import com.frolo.muse.repository.GenericMediaRepository
import com.frolo.muse.repository.PlaylistRepository
import com.frolo.muse.rx.SchedulerProvider
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Completable
import io.reactivex.Flowable


class AddMediaToPlaylistUseCase @AssistedInject constructor(
        private val schedulerProvider: SchedulerProvider,
        private val playlistRepository: PlaylistRepository,
        private val genericMediaRepository: GenericMediaRepository,
        @Assisted private val items: List<Media>
) {

    fun getPlaylists(): Flowable<List<Playlist>> {
        return playlistRepository.allItems
                .subscribeOn(schedulerProvider.worker())
    }

    fun addMediaToPlaylist(
            playlist: Playlist): Completable {
        return genericMediaRepository.addToPlaylist(playlist.id, items)
                .subscribeOn(schedulerProvider.worker())
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(items: List<Media>): AddMediaToPlaylistUseCase
    }

}