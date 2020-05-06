package com.frolo.muse.interactor.media.get

import com.frolo.muse.model.media.Media
import com.frolo.muse.model.menu.SortOrderMenu
import com.frolo.muse.repository.GenericMediaRepository
import com.frolo.muse.repository.Preferences
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import javax.inject.Inject


class SearchMediaUseCase @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val repository: GenericMediaRepository,
    private val preferences: Preferences
): GetMediaUseCase<Media> {

    override fun getSortOrderMenu(): Single<SortOrderMenu> {
        return Single.error(UnsupportedOperationException())
    }

    override fun applySortOrder(sortOrder: String): Completable {
        return Completable.error(UnsupportedOperationException())
    }

    override fun applySortOrderReversed(isReversed: Boolean): Completable {
        return Completable.error(UnsupportedOperationException())
    }

    override fun getMediaList(): Flowable<List<Media>> {
        return Flowable.just(emptyList())
    }

    fun search(query: String): Flowable<List<Media>> {
        return repository.getFilteredItems(query)
                .subscribeOn(schedulerProvider.worker())
                .excludeShortAudioFiles(preferences)
    }

}