@file:Suppress("FunctionName")

package com.frolo.muse.interactor.media.get

import com.frolo.muse.model.Library
import com.frolo.muse.repository.*
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.music.model.*
import io.reactivex.Flowable


// I would make the constructor package-private but it is impossible in Kotlin
abstract class GetAllMediaUseCase<E: Media> internal constructor(
    @Library.Section section: Int,
    schedulerProvider: SchedulerProvider,
    repository: MediaRepository<E>,
    preferences: Preferences
): GetSectionedMediaUseCase<E>(section, schedulerProvider, repository, preferences)


fun GetAllSongsUseCase(
    schedulerProvider: SchedulerProvider,
    repository: SongRepository,
    preferences: Preferences
): GetAllMediaUseCase<Song> = object : GetAllMediaUseCase<Song>(
    section = Library.ALL_SONGS,
    preferences = preferences,
    repository = repository,
    schedulerProvider = schedulerProvider
) {

    override fun getSortedCollection(sortOrder: String): Flowable<List<Song>> =
        repository.getAllItems(sortOrder)

}

fun GetAllArtistsUseCase(
    schedulerProvider: SchedulerProvider,
    repository: ArtistRepository,
    preferences: Preferences
): GetAllMediaUseCase<Artist> = object : GetAllMediaUseCase<Artist>(
    section = Library.ARTISTS,
    preferences = preferences,
    repository = repository,
    schedulerProvider = schedulerProvider
) {

    override fun getSortedCollection(sortOrder: String): Flowable<List<Artist>> {
        return repository.getAllItems(sortOrder)
    }

}

fun GetAllAlbumsUseCase(
    schedulerProvider: SchedulerProvider,
    repository: AlbumRepository,
    preferences: Preferences
): GetAllMediaUseCase<Album> = object : GetAllMediaUseCase<Album>(
    section = Library.ALBUMS,
    preferences = preferences,
    repository = repository,
    schedulerProvider = schedulerProvider
) {

    override fun getSortedCollection(sortOrder: String): Flowable<List<Album>> {
        return repository.getAllItems(sortOrder)
    }

}

fun GetAllGenresUseCase(
    schedulerProvider: SchedulerProvider,
    repository: GenreRepository,
    preferences: Preferences
): GetAllMediaUseCase<Genre> = object : GetAllMediaUseCase<Genre>(
    section = Library.GENRES,
    preferences = preferences,
    repository = repository,
    schedulerProvider = schedulerProvider
) {

    override fun getSortedCollection(sortOrder: String): Flowable<List<Genre>> {
        return repository.getAllItems(sortOrder)
    }

}

fun GetAllPlaylistsUseCase(
    schedulerProvider: SchedulerProvider,
    repository: PlaylistRepository,
    preferences: Preferences
): GetAllMediaUseCase<Playlist> = object : GetAllMediaUseCase<Playlist>(
    section = Library.PLAYLISTS,
    preferences = preferences,
    repository = repository,
    schedulerProvider = schedulerProvider
) {

    override fun getSortedCollection(sortOrder: String): Flowable<List<Playlist>> =
        repository.getAllItems(sortOrder)

}