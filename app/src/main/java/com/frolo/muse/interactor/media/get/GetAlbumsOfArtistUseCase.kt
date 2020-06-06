package com.frolo.muse.interactor.media.get

import com.frolo.muse.model.media.Album
import com.frolo.muse.model.media.Artist
import com.frolo.muse.model.menu.SortOrderMenu
import com.frolo.muse.model.sort.SortOrder
import com.frolo.muse.repository.AlbumRepository
import com.frolo.muse.rx.SchedulerProvider
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
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

    @AssistedInject.Factory
    interface Factory {
        fun create(artist: Artist): GetAlbumsOfArtistUseCase
    }

}