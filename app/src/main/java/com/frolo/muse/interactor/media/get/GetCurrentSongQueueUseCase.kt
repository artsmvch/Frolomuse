package com.frolo.muse.interactor.media.get

import com.frolo.muse.engine.Player
import com.frolo.muse.model.media.Song
import com.frolo.muse.model.menu.SortOrderMenu
import com.frolo.muse.model.sort.SortOrder
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import javax.inject.Inject


class GetCurrentSongQueueUseCase @Inject constructor(
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
        return Flowable.just(
                player.getCurrentQueue().let { queue ->
                    if (queue != null) {
                        queue.snapshot
                    } else emptyList()
                }
        )
    }

}