package com.frolo.muse.ui.main.library.artists.artist.albums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.frolo.muse.di.ComponentInjector
import com.frolo.muse.di.ComponentProvider
import com.frolo.muse.router.AppRouter
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.favourite.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.favourite.GetIsFavouriteUseCase
import com.frolo.muse.interactor.media.get.GetAlbumsOfArtistUseCase
import com.frolo.muse.interactor.media.shortcut.CreateShortcutUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.music.model.Album
import com.frolo.music.model.Artist
import com.frolo.muse.permission.PermissionChecker
import com.frolo.muse.rx.SchedulerProvider
import javax.inject.Inject


class AlbumsOfArtistVMFactory constructor(
    injector: ComponentInjector,
    provider: ComponentProvider,
    artist: Artist
): ViewModelProvider.Factory {

    @Inject
    internal lateinit var permissionChecker: PermissionChecker
    /*assisted inject*/
    private lateinit var getAlbumsOfArtistUseCase: GetAlbumsOfArtistUseCase
    @Inject
    internal lateinit var getMediaMenuUseCase: GetMediaMenuUseCase<Album>
    @Inject
    internal lateinit var clickMediaUseCase: ClickMediaUseCase<Album>
    @Inject
    internal lateinit var playMediaUseCase: PlayMediaUseCase<Album>
    @Inject
    internal lateinit var shareMediaUseCase: ShareMediaUseCase<Album>
    @Inject
    internal lateinit var deleteMediaUseCase: DeleteMediaUseCase<Album>
    @Inject
    internal lateinit var getIsFavouriteUseCase: GetIsFavouriteUseCase<Album>
    @Inject
    internal lateinit var changeFavouriteUseCase: ChangeFavouriteUseCase<Album>
    @Inject
    internal lateinit var createShortcutUseCase: CreateShortcutUseCase<Album>
    @Inject
    internal lateinit var schedulerProvider: SchedulerProvider
    @Inject
    internal lateinit var appRouter: AppRouter
    @Inject
    internal lateinit var eventLogger: EventLogger

    init {
        injector.inject(this)
        getAlbumsOfArtistUseCase = provider
            .provideGetAlbumsOfArtistUseCaseFactory()
            .create(artist)
    }

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AlbumsOfArtistViewModel(
            permissionChecker,
            getAlbumsOfArtistUseCase,
            getMediaMenuUseCase,
            clickMediaUseCase,
            playMediaUseCase,
            shareMediaUseCase,
            deleteMediaUseCase,
            getIsFavouriteUseCase,
            changeFavouriteUseCase,
            createShortcutUseCase,
            schedulerProvider,
            appRouter,
            eventLogger
        ) as T
    }

}