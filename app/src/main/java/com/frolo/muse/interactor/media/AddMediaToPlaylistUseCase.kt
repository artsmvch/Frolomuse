package com.frolo.muse.interactor.media

import com.frolo.music.model.Media
import com.frolo.music.model.Playlist
import com.frolo.muse.repository.GenericMediaRepository
import com.frolo.muse.repository.PlaylistRepository
import com.frolo.muse.rx.SchedulerProvider
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Flowable
import io.reactivex.Single


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

    /**
     * Adds media [items] to the given [playlist].
     * Returns the count of added media items.
     */
    fun addMediaToPlaylist(playlist: Playlist): Single<Int> {
        return genericMediaRepository.addToPlaylist(playlist, items)
                .subscribeOn(schedulerProvider.worker())
                .andThen(Single.just(items.count()))
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(items: List<Media>): AddMediaToPlaylistUseCase
    }

}