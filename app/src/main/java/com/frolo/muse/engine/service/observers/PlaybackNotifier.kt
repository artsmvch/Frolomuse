package com.frolo.muse.engine.service.observers

import com.frolo.muse.engine.AudioSource
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.SimplePlayerObserver


/**
 * Invokes [notify] when the user should be notified about the playback.
 * The [notify] lambda takes two params: a player instance and a 'force' flag
 * that indicates whether the user should be forced to receive notification.
 * The client can do anything in the lambda, like show a notification in UI.
 */
class PlaybackNotifier constructor(
    private val notify: (player: Player, force: Boolean) -> Unit
): SimplePlayerObserver() {

    private fun notify(player: Player, force: Boolean) {
        notify.invoke(player, force)
    }

    override fun onPlaybackStarted(player: Player) {
        notify(player, true)
    }

    override fun onPlaybackPaused(player: Player) {
        notify(player, false)
    }

    override fun onAudioSourceChanged(player: Player, item: AudioSource?, positionInQueue: Int) {
        notify(player, false)
    }

}