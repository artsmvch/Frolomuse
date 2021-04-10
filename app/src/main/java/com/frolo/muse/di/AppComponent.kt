package com.frolo.muse.di

import com.frolo.muse.di.modules.*
import com.frolo.muse.engine.PlayerJournal
import com.frolo.muse.engine.service.PlayerService
import com.frolo.muse.firebase.SimpleFirebaseMessagingService
import com.frolo.muse.interactor.media.AddMediaToPlaylistUseCase
import com.frolo.muse.interactor.media.AddSongToPlaylistUseCase
import com.frolo.muse.interactor.media.get.*
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.repository.Preferences
import com.frolo.muse.repository.SongRepository
import com.frolo.muse.ui.base.BaseActivity
import com.frolo.muse.ui.main.audiofx.params.PlaybackParamsDialog
import com.frolo.muse.ui.main.audiofx.preset.SavePresetVMFactory
import com.frolo.muse.ui.main.editor.album.AlbumEditorVMFactory
import com.frolo.muse.ui.main.editor.playlist.PlaylistEditorVMFactory
import com.frolo.muse.ui.main.editor.song.SongEditorVMFactory
import com.frolo.muse.ui.main.library.albums.album.AlbumVMFactory
import com.frolo.muse.ui.main.library.artists.artist.ArtistVMFactory
import com.frolo.muse.ui.main.library.artists.artist.albums.AlbumsOfArtistVMFactory
import com.frolo.muse.ui.main.library.artists.artist.songs.SongsOfArtistVMFactory
import com.frolo.muse.ui.main.library.genres.genre.GenreVMFactory
import com.frolo.muse.ui.main.library.playlists.addmedia.AddMediaToPlaylistVMFactory
import com.frolo.muse.ui.main.library.playlists.create.CreatePlaylistVMFactory
import com.frolo.muse.ui.main.library.playlists.playlist.PlaylistVMFactory
import com.frolo.muse.ui.main.library.playlists.playlist.addsong.AddSongToPlaylistVMFactory
import com.frolo.muse.ui.main.player.lyrics.LyricsVMFactory
import com.frolo.muse.ui.main.player.poster.PosterVMFactory
import dagger.Component
import javax.inject.Singleton


@Singleton
@Component(
    modules = [
        AppModule::class,
        PlayerModule::class,
        ViewModelModule::class,
        LocalDataModule::class,
        RemoteDataModule::class,
        NavigationModule::class,
        EventLoggerModule::class,
        NetworkModule::class,
        MiscModule::class,
        UseCaseModule::class,
        SongQueueFactoryModule::class,
        UseCaseModule::class
    ]
)
interface AppComponent {

    fun provideNavigator(): Navigator
    fun providePreferences(): Preferences
    fun provideVMFactory(): ViewModelModule.ViewModelFactory
    fun provideEventLogger(): EventLogger
    fun providePlayerJournal(): PlayerJournal
    fun provideSongRepository(): SongRepository

    fun inject(service: PlayerService)

    fun inject(service: SimpleFirebaseMessagingService)

    fun inject(activity: BaseActivity)

    fun inject(fragment: PlaybackParamsDialog)

    // ViewModel factories
    fun inject(vmf: AlbumVMFactory)
    fun inject(vmf: ArtistVMFactory)
    fun inject(vmf: SongsOfArtistVMFactory)
    fun inject(vmf: AlbumsOfArtistVMFactory)
    fun inject(vmf: PlaylistVMFactory)
    fun inject(vmf: GenreVMFactory)
    fun inject(vmf: SongEditorVMFactory)
    fun inject(vmf: AlbumEditorVMFactory)
    fun inject(vmf: PlaylistEditorVMFactory)
    fun inject(vmf: PosterVMFactory)
    fun inject(vmf: AddMediaToPlaylistVMFactory)
    fun inject(vmf: AddSongToPlaylistVMFactory)
    fun inject(vmf: SavePresetVMFactory)
    fun inject(vmf: LyricsVMFactory)
    fun inject(vmf: CreatePlaylistVMFactory)

    // UseCase factories
    fun provideGetAlbumSongsUseCaseFactory(): GetAlbumSongsUseCase.Factory
    fun provideGetAlbumsOfArtistUseCaseFactory(): GetAlbumsOfArtistUseCase.Factory
    fun provideGetSongsOfArtistUseCaseFactory(): GetArtistSongsUseCase.Factory
    fun provideGetGenreSongsUseCaseFactory(): GetGenreSongsUseCase.Factory
    fun provideGetPlaylistSongsUseCaseFactory(): GetPlaylistUseCase.Factory
    fun provideAddMediaToPlaylistUseCaseFactory(): AddMediaToPlaylistUseCase.Factory
    fun provideAddSongToPlaylistUseCaseFactory(): AddSongToPlaylistUseCase.Factory

    // ViewModel factory creators
    fun providePosterVMFactoryCreator(): PosterVMFactory.Creator
}