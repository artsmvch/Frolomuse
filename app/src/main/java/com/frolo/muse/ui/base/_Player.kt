package com.frolo.muse.ui.base

import com.frolo.muse.engine.*
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
                override fun onQueueChanged(player: Player, queue: AudioSourceQueue) {
                    if (flags == OBSERVE_CURR_QUEUE) emitter.onNext(player)
                }

                override fun onAudioSourceChanged(player: Player, item: AudioSource?, positionInQueue: Int) {
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

fun Player.observeQueue(onChanged: (queue: AudioSourceQueue?) -> Unit): Disposable =
    observe(OBSERVE_CURR_QUEUE).forEach { onChanged.invoke(it.getCurrentQueue()) }