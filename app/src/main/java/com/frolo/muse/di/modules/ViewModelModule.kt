package com.frolo.muse.di.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.frolo.muse.di.ViewModelKey
import com.frolo.muse.ui.main.MainViewModel
import com.frolo.muse.ui.main.audiofx.AudioFxViewModel
import com.frolo.muse.ui.main.audiofx.params.PlaybackParamsViewModel
import com.frolo.muse.ui.main.audiofx.preset.SavePresetViewModel
import com.frolo.muse.ui.main.editor.album.AlbumEditorViewModel
import com.frolo.muse.ui.main.editor.playlist.PlaylistEditorViewModel
import com.frolo.muse.ui.main.editor.song.SongEditorViewModel
import com.frolo.muse.ui.main.library.albums.AlbumListViewModel
import com.frolo.muse.ui.main.library.albums.album.AlbumViewModel
import com.frolo.muse.ui.main.library.artists.ArtistListViewModel
import com.frolo.muse.ui.main.library.artists.artist.albums.AlbumsOfArtistViewModel
import com.frolo.muse.ui.main.library.artists.artist.songs.SongsOfArtistViewModel
import com.frolo.muse.ui.main.library.favourites.FavouriteSongListViewModel
import com.frolo.muse.ui.main.library.genres.GenreListViewModel
import com.frolo.muse.ui.main.library.genres.genre.GenreViewModel
import com.frolo.muse.ui.main.library.mostplayed.MostPlayedViewModel
import com.frolo.muse.ui.main.library.myfiles.MyFileListViewModel
import com.frolo.muse.ui.main.library.playlists.PlaylistListViewModel
import com.frolo.muse.ui.main.library.playlists.addmedia.AddMediaToPlaylistViewModel
import com.frolo.muse.ui.main.library.playlists.create.CreatePlaylistViewModel
import com.frolo.muse.ui.main.library.playlists.playlist.PlaylistViewModel
import com.frolo.muse.ui.main.library.playlists.playlist.addsong.AddSongToPlaylistViewModel
import com.frolo.muse.ui.main.library.recent.RecentlyAddedSongListViewModel
import com.frolo.muse.ui.main.library.search.SearchViewModel
import com.frolo.muse.ui.main.library.songs.SongListViewModel
import com.frolo.muse.ui.main.player.PlayerViewModel
import com.frolo.muse.ui.main.player.current.CurrentSongQueueViewModel
import com.frolo.muse.ui.main.player.lyrics.LyricsViewModel
import com.frolo.muse.ui.main.player.poster.PosterViewModel
import com.frolo.muse.ui.main.settings.hidden.HiddenFilesViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * In this module, a common VM factory provided.
 * If there is a ViewModel of a type A that depends on some static repositories only,
 * then the VM factory from this module can construct this ViewModel.
 * If a ViewModel of a type B depends on some dynamic arguments, i.e. some item id or a localized string,
 * then the VM factory from this module cannot construct the ViewModel and a custom factory will be needed.
 */
@Module
abstract class ViewModelModule {

    @Singleton
    class ViewModelFactory @Inject constructor(
            private val viewModels: MutableMap<Class<out ViewModel>, Provider<ViewModel>>
    ): ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            val provider = viewModels[modelClass]

            if (provider == null) {
                throw IllegalArgumentException(
                    "Provider for $modelClass not found. You may have forgotten to declare bind method in this module"
                )
            }

            return provider.get() as T
        }
    }

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindMainViewModel(viewModel: MainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PlayerViewModel::class)
    abstract fun bindPlayerViewModel(viewModel: PlayerViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SongListViewModel::class)
    abstract fun bindSongsViewModel(viewModel: SongListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AlbumListViewModel::class)
    abstract fun bindAlbumsViewModel(viewModel: AlbumListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ArtistListViewModel::class)
    abstract fun bindArtistListViewModel(viewModel: ArtistListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GenreListViewModel::class)
    abstract fun bindGenreListViewModel(viewModel: GenreListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FavouriteSongListViewModel::class)
    abstract fun bindFavouriteSongListViewModel(viewModel: FavouriteSongListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RecentlyAddedSongListViewModel::class)
    abstract fun bindRecentlyAddedSongListViewModel(viewModel: RecentlyAddedSongListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MyFileListViewModel::class)
    abstract fun bindMyFileListViewModel(viewModel: MyFileListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PlaylistListViewModel::class)
    abstract fun bindPlaylistListViewModel(viewModel: PlaylistListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SearchViewModel::class)
    abstract fun bindSearchViewModel(viewModel: SearchViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AudioFxViewModel::class)
    abstract fun bindAudioFxViewModel(viewModel: AudioFxViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PlaybackParamsViewModel::class)
    abstract fun bindPlaybackParamsViewModel(viewModel: PlaybackParamsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CurrentSongQueueViewModel::class)
    abstract fun bindCurrentSongQueueViewModel(viewModel: CurrentSongQueueViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HiddenFilesViewModel::class)
    abstract fun bindHiddenFilesViewModel(viewModel: HiddenFilesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MostPlayedViewModel::class)
    abstract fun bindMostPlayedViewModel(viewModel: MostPlayedViewModel): ViewModel
}