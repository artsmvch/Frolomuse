package com.frolo.muse.interactor.media.get

import com.frolo.music.model.Album
import com.frolo.music.model.Artist
import com.frolo.muse.model.menu.SortOrderMenu
import com.frolo.music.model.SortOrder
import com.frolo.music.repository.AlbumRepository
import com.frolo.muse.rx.SchedulerProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single


class GetAlbumsOfArtistUseCase @AssistedInject constructor(
    private val repository: AlbumRepository,
    private val schedulerProvider: SchedulerProvider,
    @Assisted private val artist: Artist
): GetMediaUseCase<Album> {

    override fun getSortOrderMenu(): Single<SortOrderMenu> {
        return Single.error(UnsupportedOperationException())
    }

    override fun applySortOrder(sortOrder: SortOrder): Completable {
        return Completable.error(UnsupportedOperationException())
    }

    override fun applySortOrderReversed(isReversed: Boolean): Completable {
        return Completable.error(UnsupportedOperationException())
    }

    override fun getMediaList(): Flowable<List<Album>> {
        return repository.getAlbumsOfArtist(artist)
                .subscribeOn(schedulerProvider.worker())
    }

    @AssistedFactory
    interface Factory {
        fun create(artist: Artist): GetAlbumsOfArtistUseCase
    }

}