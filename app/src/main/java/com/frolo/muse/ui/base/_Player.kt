package com.frolo.muse.ui.base

import com.frolo.muse.engine.Player
import com.frolo.muse.engine.PlayerObserver
import com.frolo.muse.engine.SimplePlayerObserver
import com.frolo.muse.engine.SongQueue
import com.frolo.muse.model.media.Song
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableOnSubscribe
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables


private const val OBSERVE_CURR_QUEUE = 0
private const val OBSERVE_CURR_SONG = 1


private fun Player.observe(flags: Int): Flowable<Player> {
    val player = this

    val flowableOnSubscribe = FlowableOnSubscribe<Player> { emitter ->
        if (!emitter.isCancelled) {
            val observer: PlayerObserver = object : SimplePlayerObserver() {
                override fun onQueueChanged(player: Player, queue: SongQueue) {
                    if (flags == OBSERVE_CURR_QUEUE) emitter.onNext(player)
                }

                override fun onSongChanged(player: Player, song: Song?, positionInQueue: Int) {
                    if (flags == OBSERVE_CURR_SONG) emitter.onNext(player)
                }
            }

            registerObserver(observer)

            emitter.setDisposable(Disposables.fromAction {
                unregisterObserver(observer)
            })
        }

        if (!emitter.isCancelled) {
            emitter.onNext(player)
        }
    }

    return Flowable.create(flowableOnSubscribe, BackpressureStrategy.LATEST)
}

fun Player.observeQueue(onChanged: (queue: SongQueue?) -> Unit): Disposable =
    observe(OBSERVE_CURR_QUEUE).forEach { onChanged.invoke(it.getCurrentQueue()) }