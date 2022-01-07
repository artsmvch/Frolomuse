package com.frolo.muse.interactor.media.get

import com.frolo.muse.common.toSongs
import com.frolo.player.Player
import com.frolo.music.model.Song
import com.frolo.muse.model.menu.SortOrderMenu
import com.frolo.music.model.SortOrder
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import javax.inject.Inject


class GetCurrentSongQueueUseCase @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val player: Player
): GetMediaUseCase<Song> {

    override fun getSortOrderMenu(): Single<SortOrderMenu> {
        return Single.error(UnsupportedOperationException())
    }

    override fun applySortOrder(sortOrder: SortOrder): Completable {
        return Completable.error(UnsupportedOperationException())
    }

    override fun applySortOrderReversed(isReversed: Boolean): Completable {
        return Completable.error(UnsupportedOperationException())
    }

    override fun getMediaList(): Flowable<List<Song>> {
        return Single.fromCallable { player.getCurrentQueue()?.snapshot?.toSongs().orEmpty() }
                .subscribeOn(schedulerProvider.computation())
                .toFlowable()
    }

}