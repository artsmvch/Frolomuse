package com.frolo.muse.interactor.media.get

import com.frolo.collections.reversedCompat
import com.frolo.muse.model.Library
import com.frolo.music.model.Media
import com.frolo.muse.model.menu.SortOrderMenu
import com.frolo.music.model.SortOrder
import com.frolo.music.repository.MediaRepository
import com.frolo.muse.repository.Preferences
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function


abstract class GetSectionedMediaUseCase <E: Media> constructor(
    @Library.Section private val section: Int,
    private val schedulerProvider: SchedulerProvider,
    private val repository: MediaRepository<E>,
    private val preferences: Preferences
): GetMediaUseCase<E> {

    abstract fun getSortedCollection(sortOrder: String): Flowable<List<E>>

    /**
     * Remove duplicates from [list] if necessary. The default implementation uses [Media.getId] to distinct items,
     * but not for sections such as [Library.FOLDERS] and [Library.MIXED].
     * If you don't need to remove duplicates, just return the given list.
     */
    open fun removeDuplicatesIfNecessary(list: List<E>): List<E> {
        return when (section) {
            // MyFiles do not have unique ID so we cannot distinct them
            Library.FOLDERS -> list
            // Mixed media list may contain media items with different type but same ID
            // so we cannot distinct them
            Library.MIXED -> list
            else -> list.distinctBy { item -> item.id }
        }
    }

    override fun getSortOrderMenu(): Single<SortOrderMenu> {
        return repository.sortOrders
            .observeOn(schedulerProvider.worker())
            .flatMap { sortOrders ->
                Single.zip(
                    preferences.getSortOrderForSection(section).firstOrError(),
                    preferences.isSortOrderReversedForSection(section).firstOrError(),
                    BiFunction { savedSortOrder: String, isReversed: Boolean ->

                        val selectedSortOrder: SortOrder? =
                                SortOrder.pick(sortOrders, savedSortOrder) ?: sortOrders.firstOrNull()

                        SortOrderMenu(sortOrders, selectedSortOrder, isReversed)
                    }
                )
            }
    }

    override fun applySortOrder(sortOrder: SortOrder): Completable {
        return preferences.saveSortOrderForSection(section, sortOrder.key)
                .subscribeOn(schedulerProvider.worker())
    }

    override fun applySortOrderReversed(isReversed: Boolean): Completable {
        return preferences.saveSortOrderReversedForSection(section, isReversed)
                .subscribeOn(schedulerProvider.worker())
    }

    override fun getMediaList(): Flowable<List<E>> {
        val sources = listOf(
            preferences.getSortOrderForSection(section),
            preferences.isSortOrderReversedForSection(section)
        )

        val combiner = Function<Array<Any>, Pair<String, Boolean>> { arr ->
            (arr[0] as String) to (arr[1] as Boolean)
        }

        return Flowable.combineLatest(sources, combiner)
            .switchMap { pair ->
                val sortOrder = pair.first
                val isReversed = pair.second
                getSortedCollection(sortOrder)
                    .subscribeOn(schedulerProvider.worker())
                    .observeOn(schedulerProvider.computation())
                    .map { list -> removeDuplicatesIfNecessary(list) }
                    .map { list ->
                        if (isReversed) {
                            list.reversedCompat()
                        } else list
                    }
        }
    }

}