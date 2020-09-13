package com.frolo.muse.engine.service.observers

import android.content.Context
import com.frolo.muse.common.toSong
import com.frolo.muse.engine.AudioSource
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.SimplePlayerObserver
import com.frolo.muse.engine.service.PlayerNtf
import io.reactivex.disposables.Disposable


/**
 * Invokes [notify] when the user should be notified about the playback and other player-related stuff.
 * The [notify] lambda takes two params: a player notification model and a 'force' flag
 * that indicates whether the user should be forced to receive notification.
 * The client can do anything in the lambda, like show a notification in UI.
 */
class PlayerNotifier constructor(
    private val context: Context,
    private val notify: (player: PlayerNtf, force: Boolean) -> Unit
): SimplePlayerObserver() {

    private var notificationDisposable: Disposable? = null

    private fun notify(item: AudioSource?, isPlaying: Boolean, force: Boolean) {
        notificationDisposable?.dispose()

        // The default notification that is posted first,
        // because the album art has not been loaded yet.
        val defaultPlayerNtf = PlayerNtf(item = item, art = null, isPlaying = isPlaying)

        notificationDisposable = Notifications.getPlaybackArt(context, item?.toSong())
            .map { art -> defaultPlayerNtf.copy(art = art) }
            //.onErrorReturnItem(defaultPlayerNtf)
            .doOnSubscribe {
                // When subscribed, the default notification is posted
                notify.invoke(defaultPlayerNtf, force)
            }
            .doOnSuccess { playerNtf ->
                // If successful, the result notification is posted without forcing,
                // since the default notification was posted when subscribed,
                // and from that point, the notification can be cancelled by the user.
                notify.invoke(playerNtf, false)
            }
            .ignoreElement()
            .subscribe({ /*stub*/ }, { /*stub*/ })
    }

    override fun onPlaybackStarted(player: Player) {
        notify(item = player.getCurrent(), isPlaying = true, force = true)
    }

    override fun onPlaybackPaused(player: Player) {
        notify(item = player.getCurrent(), isPlaying = false, force = false)
    }

    override fun onAudioSourceChanged(player: Player, item: AudioSource?, positionInQueue: Int) {
        notify(item = item, isPlaying = player.isPlaying(), force = true)
    }

    override fun onShutdown(player: Player) {
        notificationDisposable?.dispose()
    }

}