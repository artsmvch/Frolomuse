package com.frolo.muse.engine.service.observers

import android.content.Context
import android.graphics.Bitmap
import com.frolo.muse.common.toSong
import com.frolo.muse.engine.AudioSource
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.SimplePlayerObserver
import com.frolo.muse.engine.service.PlayerNotificationParams
import com.frolo.muse.engine.service.PlayerNotificationSender
import com.frolo.muse.interactor.media.favourite.GetIsFavouriteUseCase
import com.frolo.muse.model.media.Song
import com.frolo.muse.rx.flowable.doOnNextIndexed
import com.frolo.muse.rx.flowable.withDefaultItemDelayed
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference


/**
 * Calls [sender] when the user should be notified about the playback and other player-related stuff.
 * The [sender] takes two params: a player notification model and a 'force' flag
 * that indicates whether the user should be forced to receive notification.
 * The client can do anything in the lambda, like show a notification in UI.
 */
class PlayerNotifier constructor(
    private val context: Context,
    private val getIsFavouriteUseCase: GetIsFavouriteUseCase<Song>,
    private val sender: PlayerNotificationSender
): SimplePlayerObserver() {

    private var notificationDisposable: Disposable? = null

    private val lastPlayerNtfRef = AtomicReference<PlayerNotificationParams>(null)

    private fun notify(params: PlayerNotificationParams, force: Boolean) {
        lastPlayerNtfRef.set(params)
        sender.sendPlayerNotification(params, force)
    }

    private fun notify(item: AudioSource?, isPlaying: Boolean, force: Boolean) {
        notificationDisposable?.dispose()

        val song: Song? = item?.toSong()

        val lastPlayerNtf = lastPlayerNtfRef.get()

        val art: Bitmap?
        val isFav: Boolean

        if (lastPlayerNtf != null && item != null && item.source == lastPlayerNtf.item?.source) {
            // If the audio source item in the last player notification is equal to the new one,
            // then we assume that its art and favourite flag remain the same.
            art = lastPlayerNtf.art
            isFav = lastPlayerNtf.isFavourite
        } else {
            // Otherwise, then the art is the default one and the favourite flag is false.
            art = Arts.getDefault()
            isFav = false
        }

        // The default notification that is posted first,
        // because the album art has not been loaded yet.
        val defaultParams = PlayerNotificationParams(
            item = item,
            art = art,
            isPlaying = isPlaying,
            isFavourite = isFav
        )

        notificationDisposable = Arts.getPlaybackArt(context, song)
            .map { resultArt -> defaultParams.copy(art = resultArt) }
            .onErrorReturnItem(defaultParams)
            .flatMapPublisher { playerNtf ->
                if (song != null) {
                    getIsFavouriteUseCase.isFavourite(song)
                        .map { isFav -> playerNtf.copy(isFavourite = isFav) }
                } else {
                    // If the song is null, then we use the default notification
                    Flowable.just(playerNtf)
                }
            }
            // Give 100 ms to load the album art and the fav status.
            // If the delay timed out, then emit the default params.
            .withDefaultItemDelayed(defaultParams, 100, TimeUnit.MILLISECONDS)
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
            .onBackpressureLatest()
            .doOnNextIndexed { index, playerNtf ->
                val isFirstItem = index == 0
                // we can only force notify about the first item
                notify(playerNtf, force && isFirstItem)
            }
            .ignoreElements()
            .subscribe()
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