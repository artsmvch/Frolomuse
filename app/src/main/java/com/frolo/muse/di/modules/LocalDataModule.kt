package com.frolo.muse.di.modules

import android.content.Context
import android.os.Build
import com.frolo.muse.BuildConfig
import com.frolo.muse.di.Exec
import com.frolo.muse.di.impl.local.*
import com.frolo.muse.di.impl.sound.bass.BASSSoundResolverImpl
import com.frolo.muse.di.impl.stub.MediaFileRepositoryStub
import com.frolo.muse.model.media.*
import com.frolo.muse.repository.*
import dagger.Module
import dagger.Provides
import java.util.concurrent.Executor
import javax.inject.Singleton


@Module
class LocalDataModule {
    @Singleton
    @Provides
    fun providePreferences(context: Context): Preferences {
        return PreferencesImpl(context)
    }

    @Singleton
    @Provides
    fun provideFirebasePreferences(context: Context): FirebasePreferences {
        return FirebasePreferencesImpl(context)
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
    fun provideMediaFileMediaRepository(repository: MediaFileRepository): MediaRepository<MediaFile> {
        return repository
    }

    @Provides
    fun provideMediaRepository(repository: GenericMediaRepository): MediaRepository<Media> {
        return repository
    }
    //endregion

    @Singleton
    @Provides
    fun provideLibraryPreferences(context: Context): LibraryPreferences {
        return LibraryPreferenceImpl(context)
    }

    @Provides
    fun provideSongFilterProvider(preferences: LibraryPreferences): SongFilterProvider {
        return preferences
    }

    @Provides
    fun provideLibraryConfiguration(
        context: Context,
        songFilterProvider: SongFilterProvider,
        @Exec(Exec.Type.QUERY) executor: Executor,
    ): LibraryConfiguration {
        return LibraryConfiguration(context, songFilterProvider, executor)
    }

    @Singleton
    @Provides
    fun provideSongRepository(configuration: LibraryConfiguration): SongRepository {
        return SongRepositoryImpl(configuration)
    }

    @Provides
    fun provideSongWithPlayCountRepository(
        configuration: LibraryConfiguration,
        repository: SongRepository
    ): SongWithPlayCountRepository {
        return SongWithPlayCountRepositoryImpl(configuration, repository)
    }

    @Singleton
    @Provides
    fun provideAlbumRepository(configuration: LibraryConfiguration): AlbumRepository {
        return AlbumRepositoryImpl(configuration)
    }

    @Singleton
    @Provides
    fun provideArtistRepository(configuration: LibraryConfiguration): ArtistRepository {
        return ArtistRepositoryImpl(configuration)
    }

    @Singleton
    @Provides
    fun provideGenreRepository(configuration: LibraryConfiguration): GenreRepository {
        return GenreRepositoryImpl(configuration)
    }

    @Singleton
    @Provides
    fun provideAlbumChunkRepository(configuration: LibraryConfiguration): AlbumChunkRepository {
        return AlbumChunkRepositoryImpl(configuration)
    }

    @Singleton
    @Provides
    fun provideArtistChunkRepository(configuration: LibraryConfiguration): ArtistChunkRepository {
        return ArtistChunkRepositoryImpl(configuration)
    }

    @Singleton
    @Provides
    fun provideGenreChunkRepository(configuration: LibraryConfiguration): GenreChunkRepository {
        return GenreChunkRepositoryImpl(configuration)
    }

    @Singleton
    @Provides
    fun providePlaylistChunkRepository(configuration: LibraryConfiguration): PlaylistChunkRepository {
        return PlaylistChunkRepositoryImpl(configuration)
    }

    @Singleton
    @Provides
    fun providePlaylistRepository(configuration: LibraryConfiguration): PlaylistRepository {
        return PlaylistRepositoryImpl(configuration)
    }

    @Singleton
    @Provides
    fun provideMyFileRepository(configuration: LibraryConfiguration): MyFileRepository {
        return MyFileRepositoryImpl(configuration)
    }

    @Singleton
    @Provides
    fun provideMediaFileRepository(
        configuration: LibraryConfiguration,
        songRepository: SongRepository
    ): MediaFileRepository {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaFileRepositoryImpl(configuration, songRepository)
        } else {
            MediaFileRepositoryStub()
        }
    }

    @Singleton
    @Provides
    fun provideLyricsLocalRepository(context: Context): LyricsLocalRepository {
        return LyricsLocalRepositoryImpl(context)
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
        myFileRepository: MyFileRepository,
        mediaFileRepository: MediaFileRepository
    ): GenericMediaRepository {
        return GenericMediaRepositoryImpl(
            context,
            songRepository,
            artistRepository,
            albumRepository,
            genreRepository,
            playlistRepository,
            myFileRepository,
            mediaFileRepository
        )
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

    @Singleton
    @Provides
    fun provideTooltipManager(context: Context): TooltipManager {
        return TooltipManagerImpl(context)
    }

    @Singleton
    @Provides
    fun providePlaylistTransferPreferences(context: Context): PlaylistTransferPreferences {
        return PlaylistTransferPreferencesImpl(context)
    }
}