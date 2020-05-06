package com.frolo.muse.interactor.media.get

import com.frolo.muse.model.Library
import com.frolo.muse.model.media.Album
import com.frolo.muse.model.media.Song
import com.frolo.muse.repository.AlbumChunkRepository
import com.frolo.muse.repository.Preferences
import com.frolo.muse.rx.SchedulerProvider
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Flowable


class GetAlbumSongsUseCase @AssistedInject constructor(
    schedulerProvider: SchedulerProvider,
    private val repository: AlbumChunkRepository,
    preferences: Preferences,
    @Assisted private val album: Album
): GetSectionedMediaUseCase<Song>(Library.ALBUM, schedulerProvider, repository, preferences) {

    override fun getSortedCollection(sortOrder: String): Flowable<List<Song>> {
        return repository.getSongsFromAlbum(album, sortOrder)
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(album: Album): GetAlbumSongsUseCase
    }

}