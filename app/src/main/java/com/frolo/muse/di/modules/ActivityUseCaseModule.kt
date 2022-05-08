package com.frolo.muse.di.modules

import com.frolo.muse.di.ActivityScope
import com.frolo.player.Player
import com.frolo.muse.interactor.feature.FeaturesUseCase
import com.frolo.muse.router.AppRouter
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.favourite.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.get.*
import com.frolo.muse.interactor.player.ControlPlayerUseCase
import com.frolo.muse.interactor.rate.RatingUseCase
import com.frolo.muse.repository.*
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.music.model.*
import com.frolo.music.repository.*
import dagger.Module
import dagger.Provides


@Module
abstract class ActivityUseCaseModule {

    @Module
    companion object {

        /* **********************************
        * ***** Get All Media Use Cases *****
        * ******************************** */

        @Provides
        @JvmStatic
        fun provideGetAllArtistsUseCase(
            schedulerProvider: SchedulerProvider,
            repository: ArtistRepository,
            preferences: Preferences
        ): GetAllMediaUseCase<Artist> {
            return GetAllArtistsUseCase(schedulerProvider, repository, preferences)
        }

        @Provides
        @JvmStatic
        fun provideGetAllGenresUseCase(
            schedulerProvider: SchedulerProvider,
            repository: GenreRepository,
            preferences: Preferences
        ): GetAllMediaUseCase<Genre> {
            return GetAllGenresUseCase(schedulerProvider, repository, preferences)
        }

        @Provides
        @JvmStatic
        fun provideGetAllPlaylistsUseCase(
            schedulerProvider: SchedulerProvider,
            repository: PlaylistRepository,
            preferences: Preferences
        ): GetAllMediaUseCase<Playlist> {
            return GetAllPlaylistsUseCase(schedulerProvider, repository, preferences)
        }

        @Provides
        @JvmStatic
        fun provideGetAllAlbumsUseCase(
            schedulerProvider: SchedulerProvider,
            repository: AlbumRepository,
            preferences: Preferences
        ): GetAllMediaUseCase<Album> {
            return GetAllAlbumsUseCase(schedulerProvider, repository, preferences)
        }

        @Provides
        @JvmStatic
        fun provideGetAllSongsUseCase(
            schedulerProvider: SchedulerProvider,
            repository: SongRepository,
            preferences: Preferences
        ): GetAllMediaUseCase<Song> {
            return GetAllSongsUseCase(schedulerProvider, repository, preferences)
        }

        /* **********************************
        * **** Get Media Menu Use Cases *****
        * ******************************** */

        @Provides
        @JvmStatic
        fun provideGetMediaMenuUseCase(
            schedulerProvider: SchedulerProvider,
            genericMediaRepository: GenericMediaRepository,
            remoteConfigRepository: RemoteConfigRepository,
            player: Player
        ): GetMediaMenuUseCase<Media> {
            return GetMediaMenuUseCase<Media>(schedulerProvider, genericMediaRepository,
                    remoteConfigRepository,player)
        }

        @Provides
        @JvmStatic
        fun provideGetArtistMenuUseCase(
            schedulerProvider: SchedulerProvider,
            artistRepository: ArtistRepository,
            remoteConfigRepository: RemoteConfigRepository,
            player: Player
        ): GetMediaMenuUseCase<Artist> {
            return GetMediaMenuUseCase<Artist>(schedulerProvider, artistRepository,
                    remoteConfigRepository, player)
        }

        @Provides
        @JvmStatic
        fun provideGetAlbumMenuUseCase(
            schedulerProvider: SchedulerProvider,
            albumRepository: AlbumRepository,
            remoteConfigRepository: RemoteConfigRepository,
            player: Player
        ): GetMediaMenuUseCase<Album> {
            return GetMediaMenuUseCase<Album>(schedulerProvider, albumRepository,
                    remoteConfigRepository, player)
        }

        @Provides
        @JvmStatic
        fun provideGetGenreMenuUseCase(
            schedulerProvider: SchedulerProvider,
            genreRepository: GenreRepository,
            remoteConfigRepository: RemoteConfigRepository,
            player: Player
        ): GetMediaMenuUseCase<Genre> {
            return GetMediaMenuUseCase<Genre>(schedulerProvider, genreRepository,
                    remoteConfigRepository, player)
        }

        @Provides
        @JvmStatic
        fun provideGetPlaylistMenuUseCase(
            schedulerProvider: SchedulerProvider,
            playlistRepository: PlaylistRepository,
            remoteConfigRepository: RemoteConfigRepository,
            player: Player
        ): GetMediaMenuUseCase<Playlist> {
            return GetMediaMenuUseCase<Playlist>(schedulerProvider, playlistRepository,
                    remoteConfigRepository, player)
        }

        @Provides
        @JvmStatic
        fun provideGetSongMenuUseCase(
            schedulerProvider: SchedulerProvider,
            songRepository: SongRepository,
            remoteConfigRepository: RemoteConfigRepository,
            player: Player
        ): GetMediaMenuUseCase<Song> {
            return GetMediaMenuUseCase<Song>(schedulerProvider, songRepository,
                    remoteConfigRepository, player)
        }

        @Provides
        @JvmStatic
        fun provideGetSongWithPlayCountMenuUseCase(
            schedulerProvider: SchedulerProvider,
            songWithPlayCountRepository: SongWithPlayCountRepository,
            remoteConfigRepository: RemoteConfigRepository,
            player: Player
        ): GetMediaMenuUseCase<SongWithPlayCount> {
            return GetMediaMenuUseCase<SongWithPlayCount>(schedulerProvider,
                    songWithPlayCountRepository, remoteConfigRepository, player)
        }

        @Provides
        @JvmStatic
        fun provideGetMyFileMenuUseCase(
            schedulerProvider: SchedulerProvider,
            myFileRepository: MyFileRepository,
            remoteConfigRepository: RemoteConfigRepository,
            player: Player
        ): GetMediaMenuUseCase<MyFile> {
            return GetMediaMenuUseCase<MyFile>(schedulerProvider, myFileRepository,
                    remoteConfigRepository, player)
        }

        @Provides
        @JvmStatic
        fun provideGetMediaFileMenuUseCase(
            schedulerProvider: SchedulerProvider,
            mediaFileRepository: MediaFileRepository,
            remoteConfigRepository: RemoteConfigRepository,
            player: Player
        ): GetMediaMenuUseCase<MediaFile> {
            return GetMediaMenuUseCase<MediaFile>(schedulerProvider, mediaFileRepository,
                    remoteConfigRepository, player)
        }

        /* **********************************
        * ****** Click Media Use Cases ******
        * ******************************** */

        @Provides
        @JvmStatic
        fun provideClickMediaUseCase(
            schedulerProvider: SchedulerProvider,
            player: Player,
            repository: GenericMediaRepository,
            appRouter: AppRouter
        ): ClickMediaUseCase<Media> {
            return ClickMediaUseCase<Media>(schedulerProvider, player, repository, appRouter)
        }

        @Provides
        @JvmStatic
        fun provideClickArtistUseCase(
            schedulerProvider: SchedulerProvider,
            player: Player,
            repository: GenericMediaRepository,
            appRouter: AppRouter
        ): ClickMediaUseCase<Artist> {
            return ClickMediaUseCase<Artist>(schedulerProvider, player, repository, appRouter)
        }

        @Provides
        @JvmStatic
        fun provideClickAlbumUseCase(
            schedulerProvider: SchedulerProvider,
            player: Player,
            repository: GenericMediaRepository,
            appRouter: AppRouter
        ): ClickMediaUseCase<Album> {
            return ClickMediaUseCase<Album>(schedulerProvider, player, repository, appRouter)
        }

        @Provides
        @JvmStatic
        fun provideClickGenreUseCase(
            schedulerProvider: SchedulerProvider,
            player: Player,
            repository: GenericMediaRepository,
            appRouter: AppRouter
        ): ClickMediaUseCase<Genre> {
            return ClickMediaUseCase<Genre>(schedulerProvider, player, repository, appRouter)
        }

        @Provides
        @JvmStatic
        fun provideClickPlaylistUseCase(
            schedulerProvider: SchedulerProvider,
            player: Player,
            repository: GenericMediaRepository,
            appRouter: AppRouter
        ): ClickMediaUseCase<Playlist> {
            return ClickMediaUseCase<Playlist>(schedulerProvider, player, repository, appRouter)
        }

        @Provides
        @JvmStatic
        fun provideClickSongUseCase(
            schedulerProvider: SchedulerProvider,
            player: Player,
            repository: GenericMediaRepository,
            appRouter: AppRouter
        ): ClickMediaUseCase<Song> {
            return ClickMediaUseCase<Song>(schedulerProvider, player, repository, appRouter)
        }

        @Provides
        @JvmStatic
        fun provideClickSongWithPlayCountUseCase(
            schedulerProvider: SchedulerProvider,
            player: Player,
            repository: GenericMediaRepository,
            appRouter: AppRouter
        ): ClickMediaUseCase<SongWithPlayCount> {
            return ClickMediaUseCase<SongWithPlayCount>(schedulerProvider, player, repository, appRouter)
        }

        @Provides
        @JvmStatic
        fun provideClickMyFileUseCase(
            schedulerProvider: SchedulerProvider,
            player: Player,
            repository: GenericMediaRepository,
            appRouter: AppRouter
        ): ClickMediaUseCase<MyFile> {
            return ClickMediaUseCase<MyFile>(schedulerProvider, player, repository, appRouter)
        }

        @Provides
        @JvmStatic
        fun provideClickMediaFileUseCase(
            schedulerProvider: SchedulerProvider,
            player: Player,
            repository: GenericMediaRepository,
            appRouter: AppRouter,
        ): ClickMediaUseCase<MediaFile> {
            return ClickMediaUseCase<MediaFile>(schedulerProvider, player, repository, appRouter)
        }

        /* **********************************
        * **** Play Media Menu Use Cases ****
        * ******************************** */

        @Provides
        @JvmStatic
        fun providePlayMediaUseCase(
            schedulerProvider: SchedulerProvider,
            repository: GenericMediaRepository,
            player: Player
        ): PlayMediaUseCase<Media> {
            return PlayMediaUseCase<Media>(schedulerProvider, repository, player)
        }

        @Provides
        @JvmStatic
        fun providePlayArtistUseCase(
            schedulerProvider: SchedulerProvider,
            repository: ArtistRepository,
            player: Player
        ): PlayMediaUseCase<Artist> {
            return PlayMediaUseCase<Artist>(schedulerProvider, repository, player)
        }

        @Provides
        @JvmStatic
        fun providePlayAlbumUseCase(
            schedulerProvider: SchedulerProvider,
            repository: AlbumRepository,
            player: Player
        ): PlayMediaUseCase<Album> {
            return PlayMediaUseCase<Album>(schedulerProvider, repository, player)
        }

        @Provides
        @JvmStatic
        fun providePlayGenreUseCase(
            schedulerProvider: SchedulerProvider,
            repository: GenreRepository,
            player: Player
        ): PlayMediaUseCase<Genre> {
            return PlayMediaUseCase<Genre>(schedulerProvider, repository, player)
        }

        @Provides
        @JvmStatic
        fun providePlayPlaylistUseCase(
            schedulerProvider: SchedulerProvider,
            repository: PlaylistRepository,
            player: Player
        ): PlayMediaUseCase<Playlist> {
            return PlayMediaUseCase<Playlist>(schedulerProvider, repository, player)
        }

        @Provides
        @JvmStatic
        fun providePlaySongUseCase(
            schedulerProvider: SchedulerProvider,
            repository: SongRepository,
            player: Player
        ): PlayMediaUseCase<Song> {
            return PlayMediaUseCase<Song>(schedulerProvider, repository, player)
        }

        @Provides
        @JvmStatic
        fun providePlaySongWithPlayCountUseCase(
            schedulerProvider: SchedulerProvider,
            repository: SongWithPlayCountRepository,
            player: Player
        ): PlayMediaUseCase<SongWithPlayCount> {
            return PlayMediaUseCase<SongWithPlayCount>(schedulerProvider, repository, player)
        }

        @Provides
        @JvmStatic
        fun providePlayMyFileUseCase(
            schedulerProvider: SchedulerProvider,
            repository: MyFileRepository,
            player: Player
        ): PlayMediaUseCase<MyFile> {
            return PlayMediaUseCase<MyFile>(schedulerProvider, repository, player)
        }

        @Provides
        @JvmStatic
        fun providePlayMediaFileUseCase(
            schedulerProvider: SchedulerProvider,
            repository: MediaFileRepository,
            player: Player
        ): PlayMediaUseCase<MediaFile> {
            return PlayMediaUseCase<MediaFile>(schedulerProvider, repository, player)
        }

        /* **********************************
        * ***** Share Media Use Cases *****
        * ******************************** */

        @Provides
        @JvmStatic
        fun provideShareMediaUseCase(
            schedulerProvider: SchedulerProvider,
            repository: GenericMediaRepository,
            appRouter: AppRouter
        ): ShareMediaUseCase<Media> {
            return ShareMediaUseCase<Media>(schedulerProvider, repository, appRouter)
        }

        @Provides
        @JvmStatic
        fun provideShareArtistUseCase(
            schedulerProvider: SchedulerProvider,
            repository: ArtistRepository,
            appRouter: AppRouter
        ): ShareMediaUseCase<Artist> {
            return ShareMediaUseCase<Artist>(schedulerProvider, repository, appRouter)
        }

        @Provides
        @JvmStatic
        fun provideShareAlbumUseCase(
            schedulerProvider: SchedulerProvider,
            repository: AlbumRepository,
            appRouter: AppRouter
        ): ShareMediaUseCase<Album> {
            return ShareMediaUseCase<Album>(schedulerProvider, repository, appRouter)
        }

        @Provides
        @JvmStatic
        fun provideShareGenreUseCase(
            schedulerProvider: SchedulerProvider,
            repository: GenreRepository,
            appRouter: AppRouter
        ): ShareMediaUseCase<Genre> {
            return ShareMediaUseCase<Genre>(schedulerProvider, repository, appRouter)
        }

        @Provides
        @JvmStatic
        fun provideSharePlaylistUseCase(
            schedulerProvider: SchedulerProvider,
            repository: PlaylistRepository,
            appRouter: AppRouter
        ): ShareMediaUseCase<Playlist> {
            return ShareMediaUseCase<Playlist>(schedulerProvider, repository, appRouter)
        }

        @Provides
        @JvmStatic
        fun provideShareSongUseCase(
            schedulerProvider: SchedulerProvider,
            repository: SongRepository,
            appRouter: AppRouter
        ): ShareMediaUseCase<Song> {
            return ShareMediaUseCase<Song>(schedulerProvider, repository,appRouter)
        }

        @Provides
        @JvmStatic
        fun provideShareSongWithPlayCountUseCase(
            schedulerProvider: SchedulerProvider,
            repository: SongWithPlayCountRepository,
            appRouter: AppRouter
        ): ShareMediaUseCase<SongWithPlayCount> {
            return ShareMediaUseCase<SongWithPlayCount>(schedulerProvider, repository, appRouter)
        }

        @Provides
        @JvmStatic
        fun provideShareMyFileUseCase(
            schedulerProvider: SchedulerProvider,
            repository: MyFileRepository,
            appRouter: AppRouter
        ): ShareMediaUseCase<MyFile> {
            return ShareMediaUseCase<MyFile>(schedulerProvider, repository, appRouter)
        }

        @Provides
        @JvmStatic
        fun provideShareMediaFileUseCase(
            schedulerProvider: SchedulerProvider,
            repository: MediaFileRepository,
            appRouter: AppRouter
        ): ShareMediaUseCase<MediaFile> {
            return ShareMediaUseCase<MediaFile>(schedulerProvider, repository, appRouter)
        }

        /* **********************************
        * ***** Delete Media Use Cases ******
        * ******************************** */

        @Provides
        @JvmStatic
        fun provideDeleteMediaUseCase(
            schedulerProvider: SchedulerProvider,
            repository: GenericMediaRepository,
            playlistChunkRepository: PlaylistChunkRepository,
            player: Player
        ): DeleteMediaUseCase<Media> {
            return DeleteMediaUseCase<Media>(schedulerProvider, repository, playlistChunkRepository, player)
        }

        @Provides
        @JvmStatic
        fun provideDeleteArtistUseCase(
            schedulerProvider: SchedulerProvider,
            repository: ArtistRepository,
            playlistChunkRepository: PlaylistChunkRepository,
            player: Player
        ): DeleteMediaUseCase<Artist> {
            return DeleteMediaUseCase<Artist>(schedulerProvider, repository,playlistChunkRepository, player)
        }

        @Provides
        @JvmStatic
        fun provideDeleteAlbumUseCase(
            schedulerProvider: SchedulerProvider,
            repository: AlbumRepository,
            playlistChunkRepository: PlaylistChunkRepository,
            player: Player
        ): DeleteMediaUseCase<Album> {
            return DeleteMediaUseCase<Album>(schedulerProvider, repository, playlistChunkRepository, player)
        }

        @Provides
        @JvmStatic
        fun provideDeleteGenreUseCase(
            schedulerProvider: SchedulerProvider,
            repository: GenreRepository,
            playlistChunkRepository: PlaylistChunkRepository,
            player: Player
        ): DeleteMediaUseCase<Genre> {
            return DeleteMediaUseCase<Genre>(schedulerProvider, repository, playlistChunkRepository, player)
        }

        @Provides
        @JvmStatic
        fun provideDeletePlaylistUseCase(
            schedulerProvider: SchedulerProvider,
            repository: PlaylistRepository,
            playlistChunkRepository: PlaylistChunkRepository,
            player: Player
        ): DeleteMediaUseCase<Playlist> {
            return DeleteMediaUseCase<Playlist>(schedulerProvider, repository, playlistChunkRepository, player)
        }

        @Provides
        @JvmStatic
        fun provideDeleteSongUseCase(
            schedulerProvider: SchedulerProvider,
            repository: SongRepository,
            playlistChunkRepository: PlaylistChunkRepository,
            player: Player
        ): DeleteMediaUseCase<Song> {
            return DeleteMediaUseCase<Song>(schedulerProvider, repository, playlistChunkRepository, player)
        }

        @Provides
        @JvmStatic
        fun provideDeleteSongWithPlayCountUseCase(
            schedulerProvider: SchedulerProvider,
            repository: SongWithPlayCountRepository,
            playlistChunkRepository: PlaylistChunkRepository,
            player: Player
        ): DeleteMediaUseCase<SongWithPlayCount> {
            return DeleteMediaUseCase<SongWithPlayCount>(schedulerProvider, repository,
                    playlistChunkRepository, player)
        }

        @Provides
        @JvmStatic
        fun provideDeleteMyFileUseCase(
            schedulerProvider: SchedulerProvider,
            repository: MyFileRepository,
            playlistChunkRepository: PlaylistChunkRepository,
            player: Player
        ): DeleteMediaUseCase<MyFile> {
            return DeleteMediaUseCase<MyFile>(schedulerProvider, repository, playlistChunkRepository, player)
        }

        @Provides
        @JvmStatic
        fun provideDeleteMediaFileUseCase(
            schedulerProvider: SchedulerProvider,
            repository: MediaFileRepository,
            playlistChunkRepository: PlaylistChunkRepository,
            player: Player
        ): DeleteMediaUseCase<MediaFile> {
            return DeleteMediaUseCase<MediaFile>(schedulerProvider, repository, playlistChunkRepository, player)
        }

        /* **********************************
        * *** Change Favourite Use Cases ****
        * ******************************** */

        @Provides
        @JvmStatic
        fun provideChangeFavouriteMediaUseCase(
            repository: GenericMediaRepository
        ): ChangeFavouriteUseCase<Media> {
            return ChangeFavouriteUseCase(repository)
        }

        @Provides
        @JvmStatic
        fun provideChangeFavouriteArtistUseCase(
            repository: ArtistRepository
        ): ChangeFavouriteUseCase<Artist> {
            return ChangeFavouriteUseCase(repository)
        }

        @Provides
        @JvmStatic
        fun provideChangeFavouriteAlbumUseCase(
            repository: AlbumRepository
        ): ChangeFavouriteUseCase<Album> {
            return ChangeFavouriteUseCase(repository)
        }

        @Provides
        @JvmStatic
        fun provideChangeFavouriteGenreUseCase(
            repository: GenreRepository
        ): ChangeFavouriteUseCase<Genre> {
            return ChangeFavouriteUseCase(repository)
        }

        @Provides
        @JvmStatic
        fun provideChangeFavouritePlaylistUseCase(
            repository: PlaylistRepository
        ): ChangeFavouriteUseCase<Playlist> {
            return ChangeFavouriteUseCase(repository)
        }

        @Provides
        @JvmStatic
        fun provideChangeFavouriteSongUseCase(
            repository: SongRepository
        ): ChangeFavouriteUseCase<Song> {
            return ChangeFavouriteUseCase(repository)
        }

        @Provides
        @JvmStatic
        fun provideChangeFavouriteSongWithPlayCountUseCase(
            repository: SongWithPlayCountRepository
        ): ChangeFavouriteUseCase<SongWithPlayCount> {
            return ChangeFavouriteUseCase(repository)
        }

        @Provides
        @JvmStatic
        fun provideChangeFavouriteMyFileUseCase(
            repository: MyFileRepository
        ): ChangeFavouriteUseCase<MyFile> {
            return ChangeFavouriteUseCase(repository)
        }

        @Provides
        @JvmStatic
        fun provideChangeFavouriteMediaFileUseCase(
            repository: MediaFileRepository
        ): ChangeFavouriteUseCase<MediaFile> {
            return ChangeFavouriteUseCase(repository)
        }

        /* **********************************
        * ********* Rating Use Case *********
        * ******************************** */

        @ActivityScope
        @Provides
        @JvmStatic
        fun provideRateUseCase(
            schedulerProvider: SchedulerProvider,
            preferences: Preferences,
            appRouter: AppRouter
        ): RatingUseCase {
            return RatingUseCase(schedulerProvider, preferences, appRouter)
        }

        /* **********************************
        * ***** Control Player Use Case *****
        * ******************************** */
        @ActivityScope
        @Provides
        @JvmStatic
        fun provideControlPlayerUseCase(
            player: Player,
            schedulerProvider: SchedulerProvider,
            albumRepository: AlbumRepository,
            artistRepository: ArtistRepository
        ): ControlPlayerUseCase {
            return ControlPlayerUseCase(player, schedulerProvider, albumRepository, artistRepository)
        }

        /* **********************************
        * **** Song Play Count Use Case ****
        * ******************************** */
        @Provides
        @ActivityScope
        @JvmStatic
        fun provideDispatchSongPlayedUseCase(
            schedulerProvider: SchedulerProvider,
            songRepository: SongRepository
        ): DispatchSongPlayedUseCase {
            return DispatchSongPlayedUseCase(schedulerProvider, songRepository)
        }

        /* **********************************
        * ******** Features Use Case ********
        * ******************************** */
        @Provides
        @ActivityScope
        @JvmStatic
        fun provideFeaturesUseCase(
            remoteConfigRepository: RemoteConfigRepository,
            lyricsRemoteRepository: LyricsRemoteRepository,
            schedulerProvider: SchedulerProvider
        ): FeaturesUseCase {
            return FeaturesUseCase(remoteConfigRepository, lyricsRemoteRepository, schedulerProvider)
        }
    }

}