package com.frolo.muse.di

import androidx.lifecycle.ViewModelProvider
import com.frolo.billing.BillingManager
import com.frolo.muse.interactor.media.AddMediaToPlaylistUseCase
import com.frolo.muse.interactor.media.AddSongToPlaylistUseCase
import com.frolo.muse.interactor.media.get.*
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.repository.AppearancePreferences
import com.frolo.muse.repository.LibraryPreferences
import com.frolo.muse.repository.Preferences
import com.frolo.muse.router.AppRouter
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.main.player.poster.PosterVMFactory
import com.frolo.music.repository.SongRepository
import com.frolo.player.PlayerJournal


/**
 * Responsible for providing dependencies. This is less preferable than [ComponentInjector].
 */
interface ComponentProvider {

    // Misc
    fun provideSchedulerProvider(): SchedulerProvider
    fun provideNavigator(): AppRouter
    fun providePreferences(): Preferences
    fun provideLibraryPreferences(): LibraryPreferences
    fun provideAppearancePreferences(): AppearancePreferences
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