package com.frolo.muse.interactor.media.get

import com.frolo.muse.model.Recently
import com.frolo.muse.model.media.Song
import com.frolo.muse.model.menu.RecentPeriodMenu
import com.frolo.muse.model.menu.SortOrderMenu
import com.frolo.muse.repository.Preferences
import com.frolo.muse.repository.SongRepository
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.concurrent.TimeUnit


class GetRecentlyAddedSongUseCase constructor(
        private val schedulerProvider: SchedulerProvider,
        private val repository: SongRepository,
        private val preferences: Preferences
): GetMediaUseCase<Song> {

    // calculates timestamp in seconds
    private fun calculatePeriodLastTimestamp(@Recently.Period period: Int): Long {
        val duration = when(period) {
            Recently.FOR_LAST_HOUR -> TimeUnit.HOURS.toSeconds(1)
            Recently.FOR_LAST_DAY -> TimeUnit.DAYS.toSeconds(1)
            Recently.FOR_LAST_WEEK -> TimeUnit.DAYS.toSeconds(7)
            Recently.FOR_LAST_MONTH -> TimeUnit.DAYS.toSeconds(31)
            Recently.FOR_LAST_YEAR -> TimeUnit.DAYS.toSeconds(365) // or 366?
            else -> 0
        }
        return System.currentTimeMillis() / 1000 - duration
    }

    private fun getItemsForPeriod(@Recently.Period period: Int): Flowable<List<Song>> {
        return Single.fromCallable { calculatePeriodLastTimestamp(period) }
                .flatMapPublisher { timestamp -> repository.getRecentlyAddedSongs(timestamp) }
                .subscribeOn(schedulerProvider.worker())
    }

    fun getRecentPeriodMenu(): Single<RecentPeriodMenu> {
        val periods = listOf(
                Recently.FOR_LAST_HOUR,
                Recently.FOR_LAST_DAY,
                Recently.FOR_LAST_WEEK,
                Recently.FOR_LAST_MONTH,
                Recently.FOR_LAST_YEAR
        )
        val menu = RecentPeriodMenu(
                periods,
                preferences.recentlyAddedPeriod)

        return Single.just(menu)
    }

    fun applyPeriod(@Recently.Period period: Int): Flowable<List<Song>> {
        preferences.recentlyAddedPeriod = period

        return getItemsForPeriod(period)
    }

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
        val period = preferences.recentlyAddedPeriod

        return getItemsForPeriod(period)
    }

}