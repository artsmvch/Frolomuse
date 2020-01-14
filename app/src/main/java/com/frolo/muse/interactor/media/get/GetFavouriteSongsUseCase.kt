package com.frolo.muse.interactor.media.get

import com.frolo.muse.model.media.Song
import com.frolo.muse.model.menu.SortOrderMenu
import com.frolo.muse.repository.SongRepository
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single


class GetFavouriteSongsUseCase constructor(
        private val schedulerProvider: SchedulerProvider,
        private val repository: SongRepository
): GetMediaUseCase<Song> {

    override fun getSortOrderMenu(): Single<SortOrderMenu> {
        return Single.error(UnsupportedOperationException())
    }

    override fun applySortOrder(sortOrder: String): Completable {
        return Completable.error(UnsupportedOperationException())
    }

    override fun applySortOrderReversed(isReversed: Boolean): Completable {
        return Completable.error(UnsupportedOperationException())
    }

    override fun getMediaList(): Flowable<List<Song>> {
        return repository.allFavouriteItems
                .subscribeOn(schedulerProvider.worker())
    }


}