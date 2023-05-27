package com.frolo.muse.di.modules

import android.content.Context
import android.os.Build
import com.frolo.muse.BuildConfig
import com.frolo.muse.di.ApplicationScope
import com.frolo.muse.di.ExecutorQualifier
import com.frolo.muse.di.impl.local.*
import com.frolo.muse.di.impl.sound.bass.BASSSoundWaveResolverImpl
import com.frolo.muse.di.impl.stub.MediaFileRepositoryStub
import com.frolo.muse.graphics.PaletteGenerator
import com.frolo.muse.repository.*
import com.frolo.music.model.*
import com.frolo.music.repository.*
import dagger.Module
import dagger.Provides
import java.util.concurrent.Executor


@Module
class LocalDataModule {
    @ApplicationScope
    @Provides
    fun providePreferences(context: Context): Preferences {
        return PreferencesImpl(context)
    }

    @ApplicationScope
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
            repository: SongWithPlayCountRepository): MediaRepository<SongWithPlayCount> {
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

    @ApplicationScope
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
        @ExecutorQualifier(ExecutorQualifier.ThreadType.BACKGROUND) executor: Executor,
    ): LibraryConfiguration {
        return LibraryConfiguration(context, songFilterProvider, executor)
    }

    @ApplicationScope
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

    @ApplicationScope
    @Provides
    fun provideAlbumRepository(configuration: LibraryConfiguration): AlbumRepository {
        return AlbumRepositoryImpl(configuration)
    }

    @ApplicationScope
    @Provides
    fun provideArtistRepository(configuration: LibraryConfiguration): ArtistRepository {
        return ArtistRepositoryImpl(configuration)
    }

    @ApplicationScope
    @Provides
    fun provideGenreRepository(configuration: LibraryConfiguration): GenreRepository {
        return GenreRepositoryImpl(configuration)
    }

    @ApplicationScope
    @Provides
    fun provideAlbumChunkRepository(configuration: LibraryConfiguration): AlbumChunkRepository {
        return AlbumChunkRepositoryImpl(configuration)
    }

    @ApplicationScope
    @Provides
    fun provideArtistChunkRepository(configuration: LibraryConfiguration): ArtistChunkRepository {
        return ArtistChunkRepositoryImpl(configuration)
    }

    @ApplicationScope
    @Provides
    fun provideGenreChunkRepository(configuration: LibraryConfiguration): GenreChunkRepository {
        return GenreChunkRepositoryImpl(configuration)
    }

    @ApplicationScope
    @Provides
    fun providePlaylistChunkRepository(configuration: LibraryConfiguration): PlaylistChunkRepository {
        return PlaylistChunkRepositoryImpl(configuration)
    }

    @ApplicationScope
    @Provides
    fun providePlaylistRepository(configuration: LibraryConfiguration): PlaylistRepository {
        return PlaylistRepositoryImpl(configuration)
    }

    @ApplicationScope
    @Provides
    fun provideMyFileRepository(configuration: LibraryConfiguration): MyFileRepository {
        return MyFileRepositoryImpl(configuration)
    }

    @ApplicationScope
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

    @ApplicationScope
    @Provides
    fun provideLyricsLocalRepository(context: Context): LyricsLocalRepository {
        return LyricsLocalRepositoryImpl(context)
    }

    @ApplicationScope
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

    @ApplicationScope
    @Provides
    fun providePresetRepository(context: Context): PresetRepository {
        return PresetRepositoryImpl(context)
    }

    @ApplicationScope
    @Provides
    fun provideSoundResolver(): SoundWaveResolver {
        return BASSSoundWaveResolverImpl(BuildConfig.SOUND_WAVEFORM_LENGTH)
    }

    @ApplicationScope
    @Provides
    fun provideTooltipManager(context: Context): TooltipManager {
        return TooltipManagerImpl(context)
    }

    @ApplicationScope
    @Provides
    fun providePlaylistTransferPreferences(context: Context): PlaylistTransferPreferences {
        return PlaylistTransferPreferencesImpl(context)
    }

    @ApplicationScope
    @Provides
    fun provideAppearancePreferences(context: Context): AppearancePreferences {
        return AppearancePreferencesImpl(context)
    }

    @ApplicationScope
    @Provides
    fun provideRatingPreferences(context: Context, preferences: Preferences): RatingPreferences {
        return RatingPreferencesImpl(context, preferences)
    }

    @ApplicationScope
    @Provides
    fun provideAppLaunchInfoProvider(preferences: Preferences): AppLaunchInfoProvider {
        return preferences
    }

    @ApplicationScope
    @Provides
    fun provideOnboardingPreferences(
        context: Context,
        launchInfoProvider: AppLaunchInfoProvider
    ): OnboardingPreferences {
        return OnboardingPreferencesImpl(context, launchInfoProvider)
    }

    @ApplicationScope
    @Provides
    fun providePaletteGenerator(context: Context): PaletteGenerator {
        return PaletteGeneratorImpl(context)
    }
}