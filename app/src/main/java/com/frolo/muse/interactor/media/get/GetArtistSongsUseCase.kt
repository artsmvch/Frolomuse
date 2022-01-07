package com.frolo.muse.interactor.media.get

import com.frolo.muse.model.Library
import com.frolo.music.model.Artist
import com.frolo.music.model.Song
import com.frolo.music.repository.ArtistChunkRepository
import com.frolo.muse.repository.Preferences
import com.frolo.muse.rx.SchedulerProvider
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Flowable


class GetArtistSongsUseCase @AssistedInject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val repository: ArtistChunkRepository,
    private val preferences: Preferences,
    @Assisted private val artist: Artist
): GetSectionedMediaUseCase<Song>(Library.ARTIST, schedulerProvider, repository, preferences) {

    override fun getSortedCollection(sortOrder: String): Flowable<List<Song>> {
        return repository.getSongsFromArtist(artist, sortOrder)
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(artist: Artist): GetArtistSongsUseCase
    }

}