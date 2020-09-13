package com.frolo.muse.engine.service.observers

import android.content.Context
import com.frolo.muse.common.toSong
import com.frolo.muse.engine.AudioSource
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.SimplePlayerObserver
import com.frolo.muse.engine.service.PlayerNtf
import com.frolo.muse.interactor.media.favourite.GetIsFavouriteUseCase
import com.frolo.muse.model.media.Song
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable


/**
 * Invokes [notify] when the user should be notified about the playback and other player-related stuff.
 * The [notify] lambda takes two params: a player notification model and a 'force' flag
 * that indicates whether the user should be forced to receive notification.
 * The client can do anything in the lambda, like show a notification in UI.
 */
class PlayerNotifier constructor(
    private val context: Context,
    private val getIsFavouriteUseCase: GetIsFavouriteUseCase<Song>,
    private val notify: (player: PlayerNtf, force: Boolean) -> Unit
): SimplePlayerObserver() {

    private var notificationDisposable: Disposable? = null

    private fun notify(item: AudioSource?, isPlaying: Boolean, force: Boolean) {
        notificationDisposable?.dispose()

        val song = item?.toSong()

        // The default notification that is posted first,
        // because the album art has not been loaded yet.
        val defaultPlayerNtf = PlayerNtf(
            item = item,
            art = null,
            isPlaying = isPlaying,
            isFavourite = false
        )

        notificationDisposable = Notifications.getPlaybackArt(context, song)
            .doOnSubscribe {
                // When subscribed, the default notification is posted
                notify.invoke(defaultPlayerNtf, force)
            }
            .map { art -> defaultPlayerNtf.copy(art = art) }
            .onErrorReturnItem(defaultPlayerNtf)
            .flatMapPublisher { playerNtf ->
                if (song != null) {
                    getIsFavouriteUseCase.isFavourite(song)
                        .map { isFav -> playerNtf.copy(isFavourite = isFav) }
                } else {
                    // If the song is null, then it's obviously not favourite
                    Flowable.just(playerNtf.copy(isFavourite = false))
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { playerNtf ->
                // Each next notification is posted without forcing,
                // since the default notification was posted when subscribed,
                // and from that point, the notification can be cancelled by the user.
                notify.invoke(playerNtf, false)
            }
            .ignoreElements()
            .subscribe({ /*stub*/ }, { /*stub*/ })
    }

    override fun onPlaybackStarted(player: Player) {
        notify(item = player.getCurrent(), isPlaying = true, force = true)
    }

    override fun onPlaybackPaused(player: Player) {
        notify(item = player.getCurrent(), isPlaying = false, force = false)
    }

    override fun onAudioSourceChanged(player: Player, item: AudioSource?, positionInQueue: Int) {
        notify(item = item, isPlaying = player.isPlaying(), force = false)
    }

    override fun onShutdown(player: Player) {
        notificationDisposable?.dispose()
    }

}