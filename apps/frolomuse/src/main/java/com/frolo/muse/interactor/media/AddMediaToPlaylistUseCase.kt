package com.frolo.muse.interactor.media

import com.frolo.music.model.Media
import com.frolo.music.model.Playlist
import com.frolo.music.repository.GenericMediaRepository
import com.frolo.music.repository.PlaylistRepository
import com.frolo.muse.rx.SchedulerProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
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

    @AssistedFactory
    interface Factory {
        fun create(items: List<Media>): AddMediaToPlaylistUseCase
    }

}