package com.frolo.muse.engine

import android.content.Context
import androidx.annotation.MainThread
import com.frolo.muse.ThreadStrictMode
import com.frolo.muse.widget.PlayerWidget3Provider
import com.frolo.muse.widget.PlayerWidget4Provider


@MainThread
class ObserverRegistry constructor(
    private val serviceContext: Context,
    private val notificationSender: (player: Player, forceNotify: Boolean) -> Unit
) : PlayerObserver {

    private val observers = LinkedHashSet<PlayerObserver>(4)

    /****************************
     ******** PRIVATE API *******
     ***************************/

    private fun checkThread() {
        ThreadStrictMode.assertMain()
    }

    private fun postNotification(player: Player, forceNotify: Boolean) {
        notificationSender(player, forceNotify)
    }

    private fun updateWidgets(player: Player) {
        checkThread()
        PlayerWidget3Provider.update(serviceContext, player)
        PlayerWidget4Provider.update(serviceContext, player)
    }

    private inline fun notifyObservers(action: (observer: PlayerObserver) -> Unit) {
        observers.forEach(action)
    }

    /****************************
     ******** PUBLIC API ********
     ***************************/

    fun register(observer: PlayerObserver) {
        checkThread()
        observers.add(observer)
    }

    fun unregister(observer: PlayerObserver) {
        checkThread()
        observers.remove(observer)
    }

    fun clear() {
        checkThread()
        observers.clear()
    }

    /****************************
     ******* PUBLIC API *******
     ***************************/

    override fun onPrepared(player: Player) {
        checkThread()
        notifyObservers { it.onPrepared(player) }
    }

    override fun onPlaybackStarted(player: Player) {
        checkThread()
        updateWidgets(player)
        postNotification(player, true)
        notifyObservers { it.onPlaybackStarted(player) }
    }

    override fun onPlaybackPaused(player: Player) {
        checkThread()
        updateWidgets(player)
        postNotification(player, false)
        notifyObservers { it.onPlaybackPaused(player) }
    }

    override fun onSoughtTo(player: Player, position: Int) {
        checkThread()
        notifyObservers { it.onSoughtTo(player, position) }
    }

    override fun onQueueChanged(player: Player, queue: AudioSourceQueue) {
        checkThread()
        notifyObservers { it.onQueueChanged(player, queue) }
    }

    override fun onAudioSourceChanged(player: Player, item: AudioSource?, positionInQueue: Int) {
        checkThread()
        updateWidgets(player)
        postNotification(player, false)
        notifyObservers { it.onAudioSourceChanged(player, item, positionInQueue) }
    }

    override fun onShuffleModeChanged(player: Player, mode: Int) {
        checkThread()
        updateWidgets(player)
        notifyObservers { it.onShuffleModeChanged(player, mode) }
    }

    override fun onRepeatModeChanged(player: Player, mode: Int) {
        checkThread()
        updateWidgets(player)
        notifyObservers { it.onRepeatModeChanged(player, mode) }
    }

    override fun onShutdown(player: Player) {
        checkThread()
        notifyObservers { it.onShutdown(player) }
    }

    override fun onABChanged(player: Player, aPointed: Boolean, bPointed: Boolean) {
        checkThread()
        notifyObservers { it.onABChanged(player, aPointed, bPointed) }
    }
}