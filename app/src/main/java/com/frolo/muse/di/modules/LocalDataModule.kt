package com.frolo.muse.di.modules

import android.content.Context
import com.frolo.muse.di.Repo
import com.frolo.muse.di.impl.local.*
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

    @Singleton
    @Provides
    fun provideSongRepository(context: Context): SongRepository {
        return SongRepositoryImpl(context)
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
}