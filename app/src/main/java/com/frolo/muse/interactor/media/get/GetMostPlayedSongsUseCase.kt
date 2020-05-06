package com.frolo.muse.interactor.media.get

import com.frolo.muse.model.media.SongWithPlayCount
import com.frolo.muse.model.menu.SortOrderMenu
import com.frolo.muse.repository.Preferences
import com.frolo.muse.repository.SongWithPlayCountRepository
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import javax.inject.Inject


class GetMostPlayedSongsUseCase @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val repository: SongWithPlayCountRepository,
    private val preferences: Preferences
): GetMediaUseCase<SongWithPlayCount> {

    override fun getSortOrderMenu(): Single<SortOrderMenu> {
        return Single.error(UnsupportedOperationException())
    }

    override fun applySortOrder(sortOrder: String): Completable {
        return Completable.error(UnsupportedOperationException())
    }

    override fun applySortOrderReversed(isReversed: Boolean): Completable {
        return Completable.error(UnsupportedOperationException())
    }

    override fun getMediaList(): Flowable<List<SongWithPlayCount>> {
        return repository.allItems
                .map { list -> list.sortedByDescending { it.playCount } }
                .subscribeOn(schedulerProvider.worker())
                .excludeShortSongs(preferences)
    }

}