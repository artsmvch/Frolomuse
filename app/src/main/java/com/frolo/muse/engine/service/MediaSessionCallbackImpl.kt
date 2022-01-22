package com.frolo.muse.engine.service

import android.content.Intent
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.KeyEvent
import com.frolo.muse.common.rewindBackward
import com.frolo.muse.common.rewindForward
import com.frolo.player.Player


internal class MediaSessionCallbackImpl(private val player: Player): MediaSessionCompat.Callback() {

    override fun onPlay() {
        player.start()
    }

    private fun onToggle() {
        player.toggle()
    }

    override fun onPause() {
        player.pause()
    }

    override fun onFastForward() {
        player.rewindForward()
    }

    override fun onRewind() {
        player.rewindBackward()
    }

    override fun onSeekTo(pos: Long) {
        player.seekTo(pos.toInt())
    }

    override fun onSkipToNext() {
        player.skipToNext()
    }

    override fun onSkipToPrevious() {
        player.skipToPrevious()
    }

    override fun onSetShuffleMode(shuffleMode: Int) {
        when (shuffleMode) {
            PlaybackStateCompat.SHUFFLE_MODE_ALL,
            PlaybackStateCompat.SHUFFLE_MODE_GROUP -> {
                player.setShuffleMode(Player.SHUFFLE_ON)
            }

            PlaybackStateCompat.SHUFFLE_MODE_INVALID,
            PlaybackStateCompat.SHUFFLE_MODE_NONE -> {
                player.setShuffleMode(Player.SHUFFLE_OFF)
            }
        }
    }

    override fun onSetRepeatMode(repeatMode: Int) {
        when (repeatMode) {
            PlaybackStateCompat.REPEAT_MODE_ALL,
            PlaybackStateCompat.REPEAT_MODE_GROUP -> {
                player.setRepeatMode(Player.REPEAT_PLAYLIST)
            }

            PlaybackStateCompat.REPEAT_MODE_ONE -> {
                player.setRepeatMode(Player.REPEAT_ONE)
            }

            PlaybackStateCompat.REPEAT_MODE_INVALID,
            PlaybackStateCompat.REPEAT_MODE_NONE -> {
                player.setRepeatMode(Player.REPEAT_OFF)
            }
        }
    }

    override fun onMediaButtonEvent(mediaButtonEvent: Intent): Boolean {
        val keyEvent = mediaButtonEvent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
                ?: return false

        if (keyEvent.action != KeyEvent.ACTION_DOWN) {
            return false
        }

        return when (keyEvent.keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY,
            KeyEvent.KEYCODE_MEDIA_PAUSE,
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                // We handle all these buttons (play/pause/toggle) as toggles,
                // because some headsets dispatch events inconsistently,
                // such as multiple pause clicks in a row.
                onToggle()
                true
            }

            KeyEvent.KEYCODE_MEDIA_FAST_FORWARD,
            KeyEvent.KEYCODE_MEDIA_STEP_FORWARD -> {
                onFastForward()
                true
            }

            KeyEvent.KEYCODE_MEDIA_REWIND,
            KeyEvent.KEYCODE_MEDIA_STEP_BACKWARD -> {
                onRewind()
                true
            }

            else -> super.onMediaButtonEvent(mediaButtonEvent)
        }
    }

}