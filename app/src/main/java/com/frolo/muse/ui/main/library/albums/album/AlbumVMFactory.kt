package com.frolo.muse.ui.main.library.albums.album

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.frolo.muse.di.AppComponent
import com.frolo.muse.engine.Player
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.favourite.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.favourite.GetIsFavouriteUseCase
import com.frolo.muse.interactor.media.get.GetAlbumSongsUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.media.Album
import com.frolo.muse.model.media.Song
import com.frolo.muse.rx.SchedulerProvider
import javax.inject.Inject


class AlbumVMFactory constructor(
        appComponent: AppComponent,
        private val album: Album
): ViewModelProvider.Factory {

    @Inject
    internal lateinit var player: Player
    /* Assisted inject */
    internal lateinit var getAlbumSongsUseCase: GetAlbumSongsUseCase
    @Inject
    internal lateinit var getMediaMenuUseCase: GetMediaMenuUseCase<Song>
    @Inject
    internal lateinit var clickMediaUseCase: ClickMediaUseCase<Song>
    @Inject
    internal lateinit var playMediaUseCase: PlayMediaUseCase<Song>
    @Inject
    internal lateinit var shareMediaUseCase: ShareMediaUseCase<Song>
    @Inject
    internal lateinit var deleteMediaUseCase: DeleteMediaUseCase<Song>
    @Inject
    internal lateinit var getIsFavouriteUseCase: GetIsFavouriteUseCase<Song>
    @Inject
    internal lateinit var changeFavouriteUseCase: ChangeFavouriteUseCase<Song>
    @Inject
    internal lateinit var schedulerProvider: SchedulerProvider
    @Inject
    internal lateinit var navigator: Navigator
    @Inject
    internal lateinit var eventLogger: EventLogger

    init {
        appComponent.inject(this)
        getAlbumSongsUseCase = appComponent
                .provideGetAlbumSongsUseCaseFactory()
                .create(album)
    }

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AlbumViewModel(
                player,
                getAlbumSongsUseCase,
                getMediaMenuUseCase,
                clickMediaUseCase,
                playMediaUseCase,
                shareMediaUseCase,
                deleteMediaUseCase,
                getIsFavouriteUseCase,
                changeFavouriteUseCase,
                schedulerProvider,
                navigator,
                eventLogger,
                album
        ) as T
    }

}