package com.frolo.muse.engine.service.observers

import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import com.frolo.muse.common.toSong
import com.frolo.muse.engine.AudioSource
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.SimplePlayerObserver
import com.frolo.muse.engine.service.setMetadata
import io.reactivex.disposables.Disposable


class MediaSessionObserver constructor(
    private val context: Context,
    private val mediaSession: MediaSessionCompat
): SimplePlayerObserver() {

    private var disposable: Disposable? = null

    override fun onAudioSourceChanged(player: Player, item: AudioSource?, positionInQueue: Int) {

        disposable?.dispose()

        val song = item?.toSong()

        disposable = Arts.getPlaybackArt(context, song)
            .doOnSubscribe { mediaSession.setMetadata(song , null) }
            .doOnSuccess { art -> mediaSession.setMetadata(song , art) }
            .ignoreElement()
            .subscribe({ /* stub */}, { /* stub */ })

    }

    override fun onShutdown(player: Player) {
        disposable?.dispose()
    }

}