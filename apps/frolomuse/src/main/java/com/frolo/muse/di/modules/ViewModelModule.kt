package com.frolo.muse.di.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.frolo.muse.di.ActivityScope
import com.frolo.muse.di.ViewModelKey
import com.frolo.muse.memory.MemoryWatcher
import com.frolo.muse.memory.MemoryWatcherRegistry
import com.frolo.muse.rating.RatingViewModel
import com.frolo.muse.ui.main.MainViewModel
import com.frolo.muse.ui.main.audiofx2.params.PlaybackParamsViewModel
import com.frolo.muse.ui.main.library.LibraryViewModel
import com.frolo.muse.ui.main.library.albums.AlbumListViewModel
import com.frolo.muse.ui.main.library.artists.ArtistListViewModel
import com.frolo.muse.ui.main.library.buckets.AudioBucketListViewModel
import com.frolo.muse.ui.main.library.favourites.FavouriteSongListViewModel
import com.frolo.muse.ui.main.library.genres.GenreListViewModel
import com.frolo.muse.ui.main.library.mostplayed.MostPlayedViewModel
import com.frolo.muse.ui.main.library.myfiles.MyFileListViewModel
import com.frolo.muse.ui.main.library.playlists.PlaylistListViewModel
import com.frolo.muse.ui.main.library.recent.RecentlyAddedSongListViewModel
import com.frolo.muse.ui.main.library.search.SearchViewModel
import com.frolo.muse.ui.main.library.songs.SongListViewModel
import com.frolo.muse.ui.main.player.PlayerViewModel
import com.frolo.muse.ui.main.player.current.CurrSongQueueViewModel
import com.frolo.muse.ui.main.player.mini.MiniPlayerViewModel
import com.frolo.muse.ui.main.settings.SettingsViewModel
import com.frolo.muse.ui.main.settings.donations.DonationsViewModel
import com.frolo.muse.ui.main.settings.playback.PlaybackFadingViewModel
import com.frolo.muse.ui.main.settings.library.duration.MinAudioFileDurationViewModel
import com.frolo.muse.ui.main.settings.hidden.HiddenFilesViewModel
import com.frolo.muse.ui.main.settings.journal.PlayerJournalViewModel
import com.frolo.muse.ui.main.settings.library.filter.LibrarySongFilterViewModel
import com.frolo.muse.ui.main.settings.theme.ThemeChooserViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import javax.inject.Inject
import javax.inject.Provider

/**
 * In this module, a shared ViewModel factory is provided.
 *
 * The factory from this module can only create view model instances that depend
 * on the instances provided in the same graph.
 *
 * If you need to create a view model that depends on a dynamic argument,
 * that is not provided by the graph (some string value etc.),
 * then a custom factory is required.
 */
@Module
abstract class ViewModelModule {

    @ActivityScope
    class ViewModelFactory @Inject constructor(
        private val providers: MutableMap<Class<out ViewModel>, Provider<ViewModel>>,
        private val memoryWatcherRegistry: MemoryWatcherRegistry
    ): ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST", "FoldInitializerAndIfToElvis")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val provider = providers[modelClass]

            if (provider == null) {
                throw IllegalArgumentException("Provider for $modelClass not found. " +
                        "You may have forgotten to declare a bind method in this module")
            }

            val instance = provider.get()

            if (!modelClass.isInstance(instance)) {
                throw IllegalArgumentException("Provider returned a view model of wrong type: " +
                        "expected $modelClass, but got ${instance.javaClass}. " +
                        "Check ${ViewModelKey::class} annotation on the bind method.")
            }

            if (instance is MemoryWatcher) {
                memoryWatcherRegistry.addWeakWatcher(instance)
            }

            return instance as T
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
    @ViewModelKey(MiniPlayerViewModel::class)
    abstract fun bindMiniPlayerViewModel(viewModel: MiniPlayerViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LibraryViewModel::class)
    abstract fun bindLibraryViewModel(viewModel: LibraryViewModel): ViewModel

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
    @ViewModelKey(AudioBucketListViewModel::class)
    abstract fun bindAudioBucketListViewModel(viewModel: AudioBucketListViewModel): ViewModel

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
    @ViewModelKey(PlaybackParamsViewModel::class)
    abstract fun bindPlaybackParamsViewModel(viewModel: PlaybackParamsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CurrSongQueueViewModel::class)
    abstract fun bindCurrSongQueueViewModel(viewModel: CurrSongQueueViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    abstract fun bindBillingViewModel(viewModel: SettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HiddenFilesViewModel::class)
    abstract fun bindHiddenFilesViewModel(viewModel: HiddenFilesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MostPlayedViewModel::class)
    abstract fun bindMostPlayedViewModel(viewModel: MostPlayedViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MinAudioFileDurationViewModel::class)
    abstract fun bindMinAudioFileDurationViewModel(viewModel: MinAudioFileDurationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PlaybackFadingViewModel::class)
    abstract fun bindPlaybackFadingViewModel(viewModel: PlaybackFadingViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PlayerJournalViewModel::class)
    abstract fun bindPlayerJournalViewModel(viewModel: PlayerJournalViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ThemeChooserViewModel::class)
    abstract fun bindThemeChooserViewModel(viewModel: ThemeChooserViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LibrarySongFilterViewModel::class)
    abstract fun bindLibrarySongFilterViewModel(viewModel: LibrarySongFilterViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DonationsViewModel::class)
    abstract fun bindDonationsViewModel(viewModel: DonationsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RatingViewModel::class)
    abstract fun bindRatingViewModel(viewModel: RatingViewModel): ViewModel

    companion object {
        @JvmStatic
        @Provides
        fun provideViewModelFactory(sharedFactory: ViewModelFactory): ViewModelProvider.Factory {
            return sharedFactory
        }
    }

}