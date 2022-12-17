package com.frolo.muse.interactor.media

import com.frolo.muse.rx.SchedulerProvider
import com.frolo.music.model.Media
import com.frolo.music.model.Playlist
import com.frolo.music.repository.GenericMediaRepository
import com.frolo.music.repository.PlaylistRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
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

    /**
     * Adds media [items] to each of the given [playlists].
     */
    fun addMediaToPlaylists(playlists: Collection<Playlist>): Completable {
        val source = Completable.mergeDelayError(
            playlists.map { playlist ->
                genericMediaRepository.addToPlaylist(playlist, items)
                    .subscribeOn(schedulerProvider.worker())
            }
        )
        return source
    }

    @AssistedFactory
    interface Factory {
        fun create(items: List<Media>): AddMediaToPlaylistUseCase
    }

}