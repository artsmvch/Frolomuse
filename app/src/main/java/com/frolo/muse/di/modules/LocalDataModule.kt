package com.frolo.muse.di.modules

import android.content.Context
import com.frolo.muse.BuildConfig
import com.frolo.muse.di.Repo
import com.frolo.muse.di.impl.local.*
import com.frolo.muse.di.impl.sound.bass.BASSSoundResolverImpl
import com.frolo.muse.model.media.*
import com.frolo.muse.repository.*
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class LocalDataModule {
    @Singleton
    @Provides
    fun providePreferences(context: Context): Preferences {
        return PreferencesImpl(context)
    }

    //region Generic media repositories
    @Provides
    fun provideSongMediaRepository(repository: SongRepository): MediaRepository<Song> {
        return repository
    }

    @Provides
    fun provideSongWithPlayCountMediaRepository(
            repository: SongWithPlayCountRepository
    ): MediaRepository<SongWithPlayCount> {
        return repository
    }

    @Provides
    fun provideAlbumMediaRepository(repository: AlbumRepository): MediaRepository<Album> {
        return repository
    }

    @Provides
    fun provideArtistMediaRepository(repository: ArtistRepository): MediaRepository<Artist> {
        return repository
    }

    @Provides
    fun provideGenreMediaRepository(repository: GenreRepository): MediaRepository<Genre> {
        return repository
    }

    @Provides
    fun providePlaylistMediaRepository(repository: PlaylistRepository): MediaRepository<Playlist> {
        return repository
    }

    @Provides
    fun provideMyFileMediaRepository(repository: MyFileRepository): MediaRepository<MyFile> {
        return repository
    }

    @Provides
    fun provideMediaRepository(repository: GenericMediaRepository): MediaRepository<Media> {
        return repository
    }
    //endregion

    @Singleton
    @Provides
    fun provideSongRepository(context: Context): SongRepository {
        return SongRepositoryImpl(context)
    }

    @Provides
    fun provideSongWithPlayCountRepository(
            context: Context,
            repository: SongRepository
    ): SongWithPlayCountRepository {
        return SongWithPlayCountRepositoryImpl(context, repository)
    }

    @Singleton
    @Provides
    fun provideAlbumRepository(context: Context): AlbumRepository {
        return AlbumRepositoryImpl(context)
    }

    @Singleton
    @Provides
    fun provideArtistRepository(context: Context): ArtistRepository {
        return ArtistRepositoryImpl(context)
    }

    @Singleton
    @Provides
    fun provideGenreRepository(context: Context): GenreRepository {
        return GenreRepositoryImpl(context)
    }

    @Singleton
    @Provides
    fun provideAlbumChunkRepository(context: Context): AlbumChunkRepository {
        return AlbumChunkRepositoryImpl(context)
    }

    @Singleton
    @Provides
    fun provideArtistChunkRepository(context: Context): ArtistChunkRepository {
        return ArtistChunkRepositoryImpl(context)
    }

    @Singleton
    @Provides
    fun provideGenreChunkRepository(context: Context): GenreChunkRepository {
        return GenreChunkRepositoryImpl(context)
    }

    @Singleton
    @Provides
    fun providePlaylistChunkRepository(context: Context): PlaylistChunkRepository {
        return PlaylistChunkRepositoryImpl(context)
    }

    @Singleton
    @Provides
    fun providePlaylistRepository(context: Context): PlaylistRepository {
        return PlaylistRepositoryImpl(context)
    }

    @Singleton
    @Provides
    fun provideMyFileRepository(context: Context): MyFileRepository {
        return MyFileRepositoryImpl(context)
    }

    @Singleton
    @Provides
    @Repo(Repo.Source.LOCAL)
    fun provideLyricsRepository(context: Context): LyricsRepository {
        return LyricsRepositoryImpl(context)
    }

    @Singleton
    @Provides
    fun provideGenericMediaRepository(
            context: Context,
            songRepository: SongRepository,
            artistRepository: ArtistRepository,
            albumRepository: AlbumRepository,
            genreRepository: GenreRepository,
            playlistRepository: PlaylistRepository,
            myFileRepository: MyFileRepository): GenericMediaRepository {
        return GenericMediaRepositoryImpl(
                context,
                songRepository,
                artistRepository,
                albumRepository,
                genreRepository,
                playlistRepository,
                myFileRepository)
    }

    @Singleton
    @Provides
    fun providePresetRepository(context: Context): PresetRepository {
        return PresetRepositoryImpl(context)
    }

    @Singleton
    @Provides
    fun provideSoundResolver(): SoundResolver {
        return BASSSoundResolverImpl(BuildConfig.SOUND_FRAME_GAIN_COUNT)
    }
}