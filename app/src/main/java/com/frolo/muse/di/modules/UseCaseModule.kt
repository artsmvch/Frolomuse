package com.frolo.muse.di.modules

import com.frolo.muse.engine.Player
import com.frolo.muse.common.AudioSourceQueueFactory
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.favourite.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.get.*
import com.frolo.muse.interactor.player.ControlPlayerUseCase
import com.frolo.muse.interactor.player.RestorePlayerStateUseCase
import com.frolo.muse.interactor.rate.RateUseCase
import com.frolo.muse.model.media.*
import com.frolo.muse.repository.*
import com.frolo.muse.rx.SchedulerProvider
import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@AssistedModule
@Module(includes = [AssistedInject_UseCaseModule::class])
abstract class UseCaseModule {

    @Module
    companion object {

        /*GetAllMediaUseCase*/
        @Provides
        @JvmStatic
        fun provideGetAllArtistsUseCase(
                schedulerProvider: SchedulerProvider,
                repository: ArtistRepository,
                preferences: Preferences
        ): GetAllMediaUseCase<Artist> {
            return GetAllArtistsUseCase(
                    schedulerProvider,
                    repository,
                    preferences
            )
        }

        @Provides
        @JvmStatic
        fun provideGetAllGenresUseCase(
                schedulerProvider: SchedulerProvider,
                repository: GenreRepository,
                preferences: Preferences
        ): GetAllMediaUseCase<Genre> {
            return GetAllGenresUseCase(
                    schedulerProvider,
                    repository,
                    preferences
            )
        }

        @Provides
        @JvmStatic
        fun provideGetAllPlaylistsUseCase(
                schedulerProvider: SchedulerProvider,
                repository: PlaylistRepository,
                preferences: Preferences
        ): GetAllMediaUseCase<Playlist> {
            return GetAllPlaylistsUseCase(
                    schedulerProvider,
                    repository,
                    preferences
            )
        }

        @Provides
        @JvmStatic
        fun provideGetAllAlbumsUseCase(
                schedulerProvider: SchedulerProvider,
                repository: AlbumRepository,
                preferences: Preferences
        ): GetAllMediaUseCase<Album> {
            return GetAllAlbumsUseCase(
                    schedulerProvider,
                    repository,
                    preferences
            )
        }

        @Provides
        @JvmStatic
        fun provideGetAllSongsUseCase(
                schedulerProvider: SchedulerProvider,
                repository: SongRepository,
                preferences: Preferences
        ): GetAllMediaUseCase<Song> {
            return GetAllSongsUseCase(
                    schedulerProvider,
                    repository,
                    preferences
            )
        }

        /*GetMediaMenuUseCase*/
        @Provides
        @JvmStatic
        fun provideGetMediaMenuUseCase(
                schedulerProvider: SchedulerProvider,
                repository: GenericMediaRepository,
                player: Player
        ): GetMediaMenuUseCase<Media> {
            return GetMediaMenuUseCase<Media>(
                    schedulerProvider,
                    repository,
                    player
            )
        }

        @Provides
        @JvmStatic
        fun provideGetArtistMenuUseCase(
                schedulerProvider: SchedulerProvider,
                repository: ArtistRepository,
                player: Player
        ): GetMediaMenuUseCase<Artist> {
            return GetMediaMenuUseCase<Artist>(
                    schedulerProvider,
                    repository,
                    player
            )
        }

        @Provides
        @JvmStatic
        fun provideGetAlbumMenuUseCase(
                schedulerProvider: SchedulerProvider,
                repository: AlbumRepository,
                player: Player
        ): GetMediaMenuUseCase<Album> {
            return GetMediaMenuUseCase<Album>(
                    schedulerProvider,
                    repository,
                    player
            )
        }

        @Provides
        @JvmStatic
        fun provideGetGenreMenuUseCase(
                schedulerProvider: SchedulerProvider,
                repository: GenreRepository,
                player: Player
        ): GetMediaMenuUseCase<Genre> {
            return GetMediaMenuUseCase<Genre>(
                    schedulerProvider,
                    repository,
                    player
            )
        }

        @Provides
        @JvmStatic
        fun provideGetPlaylistMenuUseCase(
                schedulerProvider: SchedulerProvider,
                repository: PlaylistRepository,
                player: Player
        ): GetMediaMenuUseCase<Playlist> {
            return GetMediaMenuUseCase<Playlist>(
                    schedulerProvider,
                    repository,
                    player
            )
        }

        @Provides
        @JvmStatic
        fun provideGetSongMenuUseCase(
                schedulerProvider: SchedulerProvider,
                repository: SongRepository,
                player: Player
        ): GetMediaMenuUseCase<Song> {
            return GetMediaMenuUseCase<Song>(
                    schedulerProvider,
                    repository,
                    player
            )
        }

        @Provides
        @JvmStatic
        fun provideGetSongWithPlayCountMenuUseCase(
                schedulerProvider: SchedulerProvider,
                repository: SongWithPlayCountRepository,
                player: Player
        ): GetMediaMenuUseCase<SongWithPlayCount> {
            return GetMediaMenuUseCase<SongWithPlayCount>(
                    schedulerProvider,
                    repository,
                    player
            )
        }

        @Provides
        @JvmStatic
        fun provideGetMyFileMenuUseCase(
                schedulerProvider: SchedulerProvider,
                repository: MyFileRepository,
                player: Player
        ): GetMediaMenuUseCase<MyFile> {
            return GetMediaMenuUseCase<MyFile>(
                    schedulerProvider,
                    repository,
                    player
            )
        }

        /*ClickMediaUseCase*/
        @Provides
        @JvmStatic
        fun provideClickMediaUseCase(
                schedulerProvider: SchedulerProvider,
                player: Player,
                repository: GenericMediaRepository,
                navigator: Navigator,
                audioSourceQueueFactory: AudioSourceQueueFactory
        ): ClickMediaUseCase<Media> {
            return ClickMediaUseCase<Media>(
                    schedulerProvider,
                    player,
                    repository,
                    navigator,
                    audioSourceQueueFactory
            )
        }

        @Provides
        @JvmStatic
        fun provideClickArtistUseCase(
                schedulerProvider: SchedulerProvider,
                player: Player,
                repository: GenericMediaRepository,
                navigator: Navigator,
                audioSourceQueueFactory: AudioSourceQueueFactory
        ): ClickMediaUseCase<Artist> {
            return ClickMediaUseCase<Artist>(
                    schedulerProvider,
                    player,
                    repository,
                    navigator,
                    audioSourceQueueFactory
            )
        }

        @Provides
        @JvmStatic
        fun provideClickAlbumUseCase(
                schedulerProvider: SchedulerProvider,
                player: Player,
                repository: GenericMediaRepository,
                navigator: Navigator,
                audioSourceQueueFactory: AudioSourceQueueFactory
        ): ClickMediaUseCase<Album> {
            return ClickMediaUseCase<Album>(
                    schedulerProvider,
                    player,
                    repository,
                    navigator,
                    audioSourceQueueFactory
            )
        }

        @Provides
        @JvmStatic
        fun provideClickGenreUseCase(
                schedulerProvider: SchedulerProvider,
                player: Player,
                repository: GenericMediaRepository,
                navigator: Navigator,
                audioSourceQueueFactory: AudioSourceQueueFactory
        ): ClickMediaUseCase<Genre> {
            return ClickMediaUseCase<Genre>(
                    schedulerProvider,
                    player,
                    repository,
                    navigator,
                    audioSourceQueueFactory
            )
        }

        @Provides
        @JvmStatic
        fun provideClickPlaylistUseCase(
                schedulerProvider: SchedulerProvider,
                player: Player,
                repository: GenericMediaRepository,
                navigator: Navigator,
                audioSourceQueueFactory: AudioSourceQueueFactory
        ): ClickMediaUseCase<Playlist> {
            return ClickMediaUseCase<Playlist>(
                    schedulerProvider,
                    player,
                    repository,
                    navigator,
                    audioSourceQueueFactory
            )
        }

        @Provides
        @JvmStatic
        fun provideClickSongUseCase(
                schedulerProvider: SchedulerProvider,
                player: Player,
                repository: GenericMediaRepository,
                navigator: Navigator,
                audioSourceQueueFactory: AudioSourceQueueFactory
        ): ClickMediaUseCase<Song> {
            return ClickMediaUseCase<Song>(
                    schedulerProvider,
                    player,
                    repository,
                    navigator,
                    audioSourceQueueFactory
            )
        }

        @Provides
        @JvmStatic
        fun provideClickSongWithPlayCountUseCase(
                schedulerProvider: SchedulerProvider,
                player: Player,
                repository: GenericMediaRepository,
                navigator: Navigator,
                audioSourceQueueFactory: AudioSourceQueueFactory
        ): ClickMediaUseCase<SongWithPlayCount> {
            return ClickMediaUseCase<SongWithPlayCount>(
                    schedulerProvider,
                    player,
                    repository,
                    navigator,
                    audioSourceQueueFactory
            )
        }

        @Provides
        @JvmStatic
        fun provideClickMyFileUseCase(
                schedulerProvider: SchedulerProvider,
                player: Player,
                repository: GenericMediaRepository,
                navigator: Navigator,
                audioSourceQueueFactory: AudioSourceQueueFactory
        ): ClickMediaUseCase<MyFile> {
            return ClickMediaUseCase<MyFile>(
                    schedulerProvider,
                    player,
                    repository,
                    navigator,
                    audioSourceQueueFactory
            )
        }

        /*PlayMediaUseCase*/
        @Provides
        @JvmStatic
        fun providePlayMediaUseCase(
                schedulerProvider: SchedulerProvider,
                repository: GenericMediaRepository,
                preferences: Preferences,
                player: Player,
                audioSourceQueueFactory: AudioSourceQueueFactory
        ): PlayMediaUseCase<Media> {
            return PlayMediaUseCase<Media>(
                    schedulerProvider,
                    repository,
                    preferences,
                    player,
                    audioSourceQueueFactory
            )
        }

        @Provides
        @JvmStatic
        fun providePlayArtistUseCase(
                schedulerProvider: SchedulerProvider,
                repository: ArtistRepository,
                preferences: Preferences,
                player: Player,
                audioSourceQueueFactory: AudioSourceQueueFactory
        ): PlayMediaUseCase<Artist> {
            return PlayMediaUseCase<Artist>(
                    schedulerProvider,
                    repository,
                    preferences,
                    player,
                    audioSourceQueueFactory
            )
        }

        @Provides
        @JvmStatic
        fun providePlayAlbumUseCase(
                schedulerProvider: SchedulerProvider,
                repository: AlbumRepository,
                preferences: Preferences,
                player: Player,
                audioSourceQueueFactory: AudioSourceQueueFactory
        ): PlayMediaUseCase<Album> {
            return PlayMediaUseCase<Album>(
                    schedulerProvider,
                    repository,
                    preferences,
                    player,
                    audioSourceQueueFactory
            )
        }

        @Provides
        @JvmStatic
        fun providePlayGenreUseCase(
                schedulerProvider: SchedulerProvider,
                repository: GenreRepository,
                preferences: Preferences,
                player: Player,
                audioSourceQueueFactory: AudioSourceQueueFactory
        ): PlayMediaUseCase<Genre> {
            return PlayMediaUseCase<Genre>(
                    schedulerProvider,
                    repository,
                    preferences,
                    player,
                    audioSourceQueueFactory
            )
        }

        @Provides
        @JvmStatic
        fun providePlayPlaylistUseCase(
                schedulerProvider: SchedulerProvider,
                repository: PlaylistRepository,
                preferences: Preferences,
                player: Player,
                audioSourceQueueFactory: AudioSourceQueueFactory
        ): PlayMediaUseCase<Playlist> {
            return PlayMediaUseCase<Playlist>(
                    schedulerProvider,
                    repository,
                    preferences,
                    player,
                    audioSourceQueueFactory
            )
        }

        @Provides
        @JvmStatic
        fun providePlaySongUseCase(
                schedulerProvider: SchedulerProvider,
                repository: SongRepository,
                preferences: Preferences,
                player: Player,
                audioSourceQueueFactory: AudioSourceQueueFactory
        ): PlayMediaUseCase<Song> {
            return PlayMediaUseCase<Song>(
                    schedulerProvider,
                    repository,
                    preferences,
                    player,
                    audioSourceQueueFactory
            )
        }

        @Provides
        @JvmStatic
        fun providePlaySongWithPlayCountUseCase(
                schedulerProvider: SchedulerProvider,
                repository: SongWithPlayCountRepository,
                preferences: Preferences,
                player: Player,
                audioSourceQueueFactory: AudioSourceQueueFactory
        ): PlayMediaUseCase<SongWithPlayCount> {
            return PlayMediaUseCase<SongWithPlayCount>(
                    schedulerProvider,
                    repository,
                    preferences,
                    player,
                    audioSourceQueueFactory
            )
        }

        @Provides
        @JvmStatic
        fun providePlayMyFileUseCase(
                schedulerProvider: SchedulerProvider,
                repository: MyFileRepository,
                preferences: Preferences,
                player: Player,
                audioSourceQueueFactory: AudioSourceQueueFactory
        ): PlayMediaUseCase<MyFile> {
            return PlayMediaUseCase<MyFile>(
                    schedulerProvider,
                    repository,
                    preferences,
                    player,
                    audioSourceQueueFactory
            )
        }

        /*ShareMediaUseCase*/
        @Provides
        @JvmStatic
        fun provideShareMediaUseCase(
                schedulerProvider: SchedulerProvider,
                repository: GenericMediaRepository,
                navigator: Navigator
        ): ShareMediaUseCase<Media> {
            return ShareMediaUseCase<Media>(
                    schedulerProvider,
                    repository,
                    navigator
            )
        }

        @Provides
        @JvmStatic
        fun provideShareArtistUseCase(
                schedulerProvider: SchedulerProvider,
                repository: ArtistRepository,
                navigator: Navigator
        ): ShareMediaUseCase<Artist> {
            return ShareMediaUseCase<Artist>(
                    schedulerProvider,
                    repository,
                    navigator
            )
        }

        @Provides
        @JvmStatic
        fun provideShareAlbumUseCase(
                schedulerProvider: SchedulerProvider,
                repository: AlbumRepository,
                navigator: Navigator
        ): ShareMediaUseCase<Album> {
            return ShareMediaUseCase<Album>(
                    schedulerProvider,
                    repository,
                    navigator
            )
        }

        @Provides
        @JvmStatic
        fun provideShareGenreUseCase(
                schedulerProvider: SchedulerProvider,
                repository: GenreRepository,
                navigator: Navigator
        ): ShareMediaUseCase<Genre> {
            return ShareMediaUseCase<Genre>(
                    schedulerProvider,
                    repository,
                    navigator
            )
        }

        @Provides
        @JvmStatic
        fun provideSharePlaylistUseCase(
                schedulerProvider: SchedulerProvider,
                repository: PlaylistRepository,
                navigator: Navigator
        ): ShareMediaUseCase<Playlist> {
            return ShareMediaUseCase<Playlist>(
                    schedulerProvider,
                    repository,
                    navigator
            )
        }

        @Provides
        @JvmStatic
        fun provideShareSongUseCase(
                schedulerProvider: SchedulerProvider,
                repository: SongRepository,
                navigator: Navigator
        ): ShareMediaUseCase<Song> {
            return ShareMediaUseCase<Song>(
                    schedulerProvider,
                    repository,
                    navigator
            )
        }

        @Provides
        @JvmStatic
        fun provideShareSongWithPlayCountUseCase(
                schedulerProvider: SchedulerProvider,
                repository: SongWithPlayCountRepository,
                navigator: Navigator
        ): ShareMediaUseCase<SongWithPlayCount> {
            return ShareMediaUseCase<SongWithPlayCount>(
                    schedulerProvider,
                    repository,
                    navigator
            )
        }

        @Provides
        @JvmStatic
        fun provideShareMyFileUseCase(
                schedulerProvider: SchedulerProvider,
                repository: MyFileRepository,
                navigator: Navigator
        ): ShareMediaUseCase<MyFile> {
            return ShareMediaUseCase<MyFile>(
                    schedulerProvider,
                    repository,
                    navigator
            )
        }

        /*DeleteMediaUseCase*/
        @Provides
        @JvmStatic
        fun provideDeleteMediaUseCase(
                schedulerProvider: SchedulerProvider,
                repository: GenericMediaRepository,
                player: Player
        ): DeleteMediaUseCase<Media> {
            return DeleteMediaUseCase<Media>(
                    schedulerProvider,
                    repository,
                    player
            )
        }

        @Provides
        @JvmStatic
        fun provideDeleteArtistUseCase(
                schedulerProvider: SchedulerProvider,
                repository: ArtistRepository,
                player: Player
        ): DeleteMediaUseCase<Artist> {
            return DeleteMediaUseCase<Artist>(
                    schedulerProvider,
                    repository,
                    player
            )
        }

        @Provides
        @JvmStatic
        fun provideDeleteAlbumUseCase(
                schedulerProvider: SchedulerProvider,
                repository: AlbumRepository,
                player: Player
        ): DeleteMediaUseCase<Album> {
            return DeleteMediaUseCase<Album>(
                    schedulerProvider,
                    repository,
                    player
            )
        }

        @Provides
        @JvmStatic
        fun provideDeleteGenreUseCase(
                schedulerProvider: SchedulerProvider,
                repository: GenreRepository,
                player: Player
        ): DeleteMediaUseCase<Genre> {
            return DeleteMediaUseCase<Genre>(
                    schedulerProvider,
                    repository,
                    player
            )
        }

        @Provides
        @JvmStatic
        fun provideDeletePlaylistUseCase(
                schedulerProvider: SchedulerProvider,
                repository: PlaylistRepository,
                player: Player
        ): DeleteMediaUseCase<Playlist> {
            return DeleteMediaUseCase<Playlist>(
                    schedulerProvider,
                    repository,
                    player
            )
        }

        @Provides
        @JvmStatic
        fun provideDeleteSongUseCase(
                schedulerProvider: SchedulerProvider,
                repository: SongRepository,
                player: Player
        ): DeleteMediaUseCase<Song> {
            return DeleteMediaUseCase<Song>(
                    schedulerProvider,
                    repository,
                    player
            )
        }

        @Provides
        @JvmStatic
        fun provideDeleteSongWithPlayCountUseCase(
                schedulerProvider: SchedulerProvider,
                repository: SongWithPlayCountRepository,
                player: Player
        ): DeleteMediaUseCase<SongWithPlayCount> {
            return DeleteMediaUseCase<SongWithPlayCount>(
                    schedulerProvider,
                    repository,
                    player
            )
        }

        @Provides
        @JvmStatic
        fun provideDeleteMyFileUseCase(
                schedulerProvider: SchedulerProvider,
                repository: MyFileRepository,
                player: Player
        ): DeleteMediaUseCase<MyFile> {
            return DeleteMediaUseCase<MyFile>(
                    schedulerProvider,
                    repository,
                    player
            )
        }

        /*ChangeFavouriteUseCase*/
        @Provides
        @JvmStatic
        fun provideChangeFavouriteMediaUseCase(
                schedulerProvider: SchedulerProvider,
                repository: GenericMediaRepository
        ): ChangeFavouriteUseCase<Media> {
            return ChangeFavouriteUseCase(
                    schedulerProvider,
                    repository
            )
        }

        @Provides
        @JvmStatic
        fun provideChangeFavouriteArtistUseCase(
                schedulerProvider: SchedulerProvider,
                repository: ArtistRepository
        ): ChangeFavouriteUseCase<Artist> {
            return ChangeFavouriteUseCase(
                    schedulerProvider,
                    repository
            )
        }

        @Provides
        @JvmStatic
        fun provideChangeFavouriteAlbumUseCase(
                schedulerProvider: SchedulerProvider,
                repository: AlbumRepository
        ): ChangeFavouriteUseCase<Album> {
            return ChangeFavouriteUseCase(
                    schedulerProvider,
                    repository
            )
        }

        @Provides
        @JvmStatic
        fun provideChangeFavouriteGenreUseCase(
                schedulerProvider: SchedulerProvider,
                repository: GenreRepository
        ): ChangeFavouriteUseCase<Genre> {
            return ChangeFavouriteUseCase(
                    schedulerProvider,
                    repository
            )
        }

        @Provides
        @JvmStatic
        fun provideChangeFavouritePlaylistUseCase(
                schedulerProvider: SchedulerProvider,
                repository: PlaylistRepository
        ): ChangeFavouriteUseCase<Playlist> {
            return ChangeFavouriteUseCase(
                    schedulerProvider,
                    repository
            )
        }

        @Provides
        @JvmStatic
        fun provideChangeFavouriteSongUseCase(
                schedulerProvider: SchedulerProvider,
                repository: SongRepository
        ): ChangeFavouriteUseCase<Song> {
            return ChangeFavouriteUseCase(
                    schedulerProvider,
                    repository
            )
        }

        @Provides
        @JvmStatic
        fun provideChangeFavouriteSongWithPlayCountUseCase(
                schedulerProvider: SchedulerProvider,
                repository: SongWithPlayCountRepository
        ): ChangeFavouriteUseCase<SongWithPlayCount> {
            return ChangeFavouriteUseCase(
                    schedulerProvider,
                    repository
            )
        }

        @Provides
        @JvmStatic
        fun provideChangeFavouriteMyFileUseCase(
                schedulerProvider: SchedulerProvider,
                repository: MyFileRepository
        ): ChangeFavouriteUseCase<MyFile> {
            return ChangeFavouriteUseCase(
                    schedulerProvider,
                    repository
            )
        }

        /*rate*/
        @Singleton
        @Provides
        @JvmStatic
        fun provideRateUseCase(
                schedulerProvider: SchedulerProvider,
                preferences: Preferences,
                navigator: Navigator
        ): RateUseCase {
            return RateUseCase(
                    schedulerProvider,
                    preferences,
                    navigator)
        }

        /*restore player state*/
        @Singleton
        @Provides
        @JvmStatic
        fun provideRestorePlayerStateUseCase(
                schedulerProvider: SchedulerProvider,
                songRepository: SongRepository,
                albumRepository: AlbumRepository,
                artistRepository: ArtistRepository,
                genreRepository: GenreRepository,
                playlistRepository: PlaylistRepository,
                preferences: Preferences,
                audioSourceQueueFactory: AudioSourceQueueFactory
        ): RestorePlayerStateUseCase {
            return RestorePlayerStateUseCase(
                    schedulerProvider,
                    songRepository,
                    albumRepository,
                    artistRepository,
                    genreRepository,
                    playlistRepository,
                    preferences,
                    audioSourceQueueFactory)
        }

        /*control player*/
        @Singleton
        @Provides
        @JvmStatic
        fun provideControlPlayerUseCase(
                player: Player,
                schedulerProvider: SchedulerProvider,
                albumRepository: AlbumRepository,
                artistRepository: ArtistRepository
        ): ControlPlayerUseCase {
            return ControlPlayerUseCase(
                    player,
                    schedulerProvider,
                    albumRepository,
                    artistRepository)
        }

        /*Song play count*/
        @Provides
        @Singleton
        @JvmStatic
        fun provideDispatchSongPlayedUseCase(
                schedulerProvider: SchedulerProvider,
                songRepository: SongRepository
        ): DispatchSongPlayedUseCase {
            return DispatchSongPlayedUseCase(
                    schedulerProvider,
                    songRepository)
        }
    }

//    @Singleton
//    @Binds
//    abstract fun bindRestorePlayerStateUseCase(
//            useCase: RestorePlayerStateUseCase): RestorePlayerStateUseCase
//
//    @Singleton
//    @Binds
//    abstract fun bindRateUseCase(useCase: RateUseCase): RateUseCase
//
//    @Singleton
//    @Binds
//    abstract fun bindControlPlayerUseCase(
//            useCase: ControlPlayerUseCase): ControlPlayerUseCase

}