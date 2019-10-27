package com.frolo.muse.interactor.media

import com.frolo.muse.model.media.Album
import com.frolo.muse.model.media.Media
import com.frolo.muse.model.media.Playlist
import com.frolo.muse.model.media.Song
import com.frolo.muse.model.menu.ContextualMenu
import com.frolo.muse.model.menu.OptionsMenu
import com.frolo.muse.repository.MediaRepository
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Single


class GetMediaMenuUseCase<E: Media> constructor(
        private val schedulerProvider: SchedulerProvider,
        private val repository: MediaRepository<E>
) {

    fun getOptionsMenu(item: E): Single<OptionsMenu<E>> {
        // First check if the item can be favourite and if so then check if it is favourite
        val favouriteOptionOperator = if (item is Song) {
            repository.isFavourite(item)
                    .map { isFavourite -> true to isFavourite }
        } else {
            Single.just(false to false)
        }

        return favouriteOptionOperator
                .map { favouriteOption ->
                    OptionsMenu(
                            item = item,
                            favouriteOptionAvailable = favouriteOption.first,
                            isFavourite = favouriteOption.second,
                            shareOptionAvailable = true,
                            deleteOptionAvailable = true,
                            playOptionAvailable = true,
                            playNextOptionAvailable = true,
                            addToPlaylistOptionAvailable = true,
                            editOptionAvailable = item is Song || item is Album || item is Playlist,
                            addToQueueOptionAvailable = item !is Playlist,
                            viewAlbumOptionAvailable = false,//item is Song,
                            viewArtistOptionAvailable = false//item is Song || item is Album
                    )
                }
                .subscribeOn(schedulerProvider.worker())
                .observeOn(schedulerProvider.main())
    }

    fun getContextualMenu(initiator: E): Single<ContextualMenu<E>> {
        return Single.just(
                ContextualMenu(
                        targetItem = initiator,
                        selectAllOptionAvailable = true,
                        playOptionAvailable = true,
                        playNextOptionAvailable = true,
                        addToQueueOptionAvailable = true,
                        deleteOptionAvailable = true,
                        shareOptionAvailable = true,
                        addToPlaylistOptionAvailable = true
                )
        )
    }

}