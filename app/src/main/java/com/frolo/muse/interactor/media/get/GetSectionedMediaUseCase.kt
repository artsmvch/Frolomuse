package com.frolo.muse.interactor.media.get

import com.frolo.muse.model.Library
import com.frolo.muse.model.media.Media
import com.frolo.muse.model.menu.SortOrderMenu
import com.frolo.muse.repository.MediaRepository
import com.frolo.muse.repository.Preferences
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single


abstract class GetSectionedMediaUseCase <E: Media> constructor(
        @Library.Section private val section: Int,
        private val schedulerProvider: SchedulerProvider,
        private val repository: MediaRepository<E>,
        private val preferences: Preferences
): GetMediaUseCase<E> {

    abstract fun getSortedCollection(sortOrder: String): Flowable<List<E>>

    private fun getItems(sortOrder: String, isReversed: Boolean): Flowable<List<E>> {
        return getSortedCollection(sortOrder)
                .map { list ->
                    when (section) {
                        // MyFiles do not have unique ID so we cannot distinct them
                        Library.FOLDERS -> list
                        // Mixed media list may contain media items with different type but same ID
                        // so we cannot distinct them
                        Library.MIXED -> list
                        else -> list.distinctBy { item -> item.id }
                    }
                }
                .map { list ->
                    if (isReversed) {
                        list.reversed()
                    } else list
                }
                .subscribeOn(schedulerProvider.worker())
    }

    override fun getSortOrderMenu(): Single<SortOrderMenu> {
        return repository.sortOrders
                .map { sortOrders ->
                    val selectedSortOrder = preferences.getSortOrderForSection(section)
                    val sortOrderReversed = preferences.isSortOrderReversedForSection(section)
                    SortOrderMenu(sortOrders, selectedSortOrder, sortOrderReversed)
                }
                .subscribeOn(schedulerProvider.worker())
    }

    override fun applySortOrder(sortOrder: String): Flowable<List<E>> {
        return Completable.fromAction {
            preferences.saveSortOrderForSection(section, sortOrder)
        }
                .andThen(getItems(
                        sortOrder,
                        preferences.isSortOrderReversedForSection(section)))
                .subscribeOn(schedulerProvider.worker())
    }

    override fun applySortOrderReversed(isReversed: Boolean): Flowable<List<E>> {
        return Completable.fromAction {
            preferences.saveSortOrderReversedForSection(section, isReversed)
        }
                .andThen(getItems(
                        preferences.getSortOrderForSection(section),
                        isReversed)
                )
                .subscribeOn(schedulerProvider.worker())
    }

    override fun getMediaList(): Flowable<List<E>> {
        return getItems(
                preferences.getSortOrderForSection(section),
                preferences.isSortOrderReversedForSection(section))
    }

}