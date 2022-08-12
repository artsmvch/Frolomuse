package com.frolo.muse.interactor.media.get

import com.frolo.music.model.SongWithPlayCount
import com.frolo.muse.model.menu.SortOrderMenu
import com.frolo.music.model.SortOrder
import com.frolo.music.repository.SongWithPlayCountRepository
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import javax.inject.Inject


class GetMostPlayedSongsUseCase @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val repository: SongWithPlayCountRepository
): GetMediaUseCase<SongWithPlayCount> {

    override fun getSortOrderMenu(): Single<SortOrderMenu> {
        return Single.error(UnsupportedOperationException())
    }

    override fun applySortOrder(sortOrder: SortOrder): Completable {
        return Completable.error(UnsupportedOperationException())
    }

    override fun applySortOrderReversed(isReversed: Boolean): Completable {
        return Completable.error(UnsupportedOperationException())
    }

    override fun getMediaList(): Flowable<List<SongWithPlayCount>> {
        return repository.allItems
            .map { list -> list.sortedByDescending { it.playCount } }
            .subscribeOn(schedulerProvider.worker())
    }

}