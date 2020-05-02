package com.frolo.muse.interactor.media

import com.frolo.muse.navigator.Navigator
import com.frolo.muse.model.media.Playlist
import com.frolo.muse.model.media.Song
import com.frolo.muse.repository.PlaylistChunkRepository
import com.frolo.muse.repository.SongRepository
import com.frolo.muse.rx.SchedulerProvider
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single


class AddSongToPlaylistUseCase @AssistedInject constructor(
        private val schedulerProvider: SchedulerProvider,
        private val songRepository: SongRepository,
        private val playlistChunkRepository: PlaylistChunkRepository,
        private val navigator: Navigator,
        @Assisted private val playlist: Playlist
) {

    fun getTargetPlaylist(): Flowable<Playlist> = Flowable.just(playlist)

    fun search(query: String): Flowable<List<Song>> {
        return songRepository.getFilteredItems(query)
                .subscribeOn(schedulerProvider.worker())
    }

    fun addSongs(songs: Collection<Song>): Completable {
        return Single.just(songs)
                .subscribeOn(schedulerProvider.computation())
                .observeOn(schedulerProvider.worker())
                .flatMapCompletable { selectedItems ->
                    playlistChunkRepository.addToPlaylist(playlist, selectedItems)
                }
                .doOnComplete {
                    navigator.goBack()
                }
    }

    fun goBack() {
        navigator.goBack()
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(playlist: Playlist): AddSongToPlaylistUseCase
    }

}