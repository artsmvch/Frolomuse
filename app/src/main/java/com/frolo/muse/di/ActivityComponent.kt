package com.frolo.muse.di

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.frolo.billing.BillingManager
import com.frolo.muse.di.modules.*
import com.frolo.muse.interactor.media.AddMediaToPlaylistUseCase
import com.frolo.muse.interactor.media.AddSongToPlaylistUseCase
import com.frolo.muse.interactor.media.get.*
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.repository.AppearancePreferences
import com.frolo.muse.repository.LibraryPreferences
import com.frolo.muse.repository.OnboardingPreferences
import com.frolo.muse.repository.Preferences
import com.frolo.muse.router.AppRouter
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.main.audiofx.preset.SavePresetVMFactory
import com.frolo.muse.ui.main.editor.album.AlbumEditorVMFactory
import com.frolo.muse.ui.main.editor.playlist.PlaylistEditorVMFactory
import com.frolo.muse.ui.main.editor.song.SongEditorVMFactory
import com.frolo.muse.ui.main.library.albums.album.AlbumVMFactory
import com.frolo.muse.ui.main.library.artists.artist.ArtistVMFactory
import com.frolo.muse.ui.main.library.artists.artist.albums.AlbumsOfArtistVMFactory
import com.frolo.muse.ui.main.library.artists.artist.songs.SongsOfArtistVMFactory
import com.frolo.muse.ui.main.library.buckets.files.AudioBucketVMFactory
import com.frolo.muse.ui.main.library.genres.genre.GenreVMFactory
import com.frolo.muse.ui.main.library.playlists.addmedia.AddMediaToPlaylistVMFactory
import com.frolo.muse.ui.main.library.playlists.create.CreatePlaylistVMFactory
import com.frolo.muse.ui.main.library.playlists.playlist.PlaylistVMFactory
import com.frolo.muse.ui.main.library.playlists.playlist.addsong.AddSongToPlaylistVMFactory
import com.frolo.muse.ui.main.player.lyrics.LyricsVMFactory
import com.frolo.muse.ui.main.player.poster.PosterVMFactory
import com.frolo.muse.ui.main.settings.premium.BuyPremiumVMFactory
import com.frolo.music.repository.SongRepository
import com.frolo.player.PlayerJournal
import dagger.Subcomponent
import java.lang.IllegalArgumentException


/**
 * Responsible for dependency injection. This is preferred over [ActivityComponentProvider].
 */
interface ActivityComponentInjector {
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
    fun inject(vmf: AudioBucketVMFactory)
    fun inject(vmf: BuyPremiumVMFactory)
}

/**
 * Responsible for providing dependencies. This is less preferable than [ActivityComponentInjector].
 */
interface ActivityComponentProvider {
    // Misc
    fun provideSchedulerProvider(): SchedulerProvider
    fun provideAppRouter(): AppRouter
    fun providePreferences(): Preferences
    fun provideLibraryPreferences(): LibraryPreferences
    fun provideAppearancePreferences(): AppearancePreferences
    fun provideOnboardingPreferences(): OnboardingPreferences
    fun provideViewModelFactory(): ViewModelProvider.Factory
    fun provideEventLogger(): EventLogger
    fun providePlayerJournal(): PlayerJournal
    fun provideSongRepository(): SongRepository

    // UseCase factories
    fun provideGetAlbumSongsUseCaseFactory(): GetAlbumSongsUseCase.Factory
    fun provideGetAlbumsOfArtistUseCaseFactory(): GetAlbumsOfArtistUseCase.Factory
    fun provideGetSongsOfArtistUseCaseFactory(): GetArtistSongsUseCase.Factory
    fun provideGetGenreSongsUseCaseFactory(): GetGenreSongsUseCase.Factory
    fun provideGetPlaylistSongsUseCaseFactory(): GetPlaylistUseCase.Factory
    fun provideAddMediaToPlaylistUseCaseFactory(): AddMediaToPlaylistUseCase.Factory
    fun provideAddSongToPlaylistUseCaseFactory(): AddSongToPlaylistUseCase.Factory
    fun provideExploreMediaBucketUseCaseFactory(): ExploreMediaBucketUseCase.Factory

    // ViewModel factory creators
    fun providePosterVMFactoryCreator(): PosterVMFactory.Creator

    // Billing
    fun provideBillingManager(): BillingManager
}

@ActivityScope
@Subcomponent(
    modules = [
        ActivityModule::class,
        ActivityUseCaseModule::class,
        ViewModelModule::class
    ]
)
interface ActivityComponent : ActivityComponentInjector, ActivityComponentProvider

interface ActivityComponentHolder {
    val activityComponent: ActivityComponent
}

val Fragment.activityComponent: ActivityComponent
    get() {
        val safeActivity = this.activity
            ?: throw NullPointerException("$this not attached to an Activity")

        if (safeActivity !is ActivityComponentHolder) {
            throw IllegalArgumentException("$safeActivity is not a component holder")
        }

        return safeActivity.activityComponent
    }