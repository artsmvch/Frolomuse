package com.frolo.muse.interactor.media.get

import com.frolo.muse.model.media.Song
import com.frolo.muse.model.menu.SortOrderMenu
import com.frolo.muse.repository.Preferences
import com.frolo.muse.repository.SongRepository
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import javax.inject.Inject


class GetFavouriteSongsUseCase @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val repository: SongRepository,
    private val preferences: Preferences
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
                .excludeShortSongs(preferences)
    }


}