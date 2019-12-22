package com.frolo.muse.ui.main.library.artists.artist.albums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.frolo.muse.di.AppComponent
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.favourite.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.favourite.GetIsFavouriteUseCase
import com.frolo.muse.interactor.media.get.GetAlbumsOfArtistUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.media.Album
import com.frolo.muse.model.media.Artist
import com.frolo.muse.rx.SchedulerProvider
import javax.inject.Inject


class AlbumsOfArtistVMFactory constructor(
        appComponent: AppComponent,
        artist: Artist
): ViewModelProvider.Factory {

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
    internal lateinit var schedulerProvider: SchedulerProvider
    @Inject
    internal lateinit var navigator: Navigator
    @Inject
    internal lateinit var eventLogger: EventLogger

    init {
        appComponent.inject(this)
        getAlbumsOfArtistUseCase = appComponent
                .provideGetAlbumsOfArtistUseCaseFactory()
                .create(artist)
    }

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AlbumsOfArtistViewModel(
                getAlbumsOfArtistUseCase,
                getMediaMenuUseCase,
                clickMediaUseCase,
                playMediaUseCase,
                shareMediaUseCase,
                deleteMediaUseCase,
                getIsFavouriteUseCase,
                changeFavouriteUseCase,
                schedulerProvider,
                navigator,
                eventLogger
        ) as T
    }

}