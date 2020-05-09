package com.frolo.muse.engine

import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import com.frolo.muse.Logger


class AudioFocusHandler constructor(
        private val audioManager: AudioManager?,
        private val player: Player
) : AudioManager.OnAudioFocusChangeListener {

    companion object {
        private const val LOG_TAG = "AudioFocusHandler"
    }

    private var audioFocusRequest: AudioFocusRequest? = null

    // This flag is used to remember the last playback state.
    // So we can know if the player was playing before the audio focus loss.
    // And then resume the playback again if needed.
    private var wasPlaying = false

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Pause playback because your Audio Focus was
                // temporarily stolen, but will be back soon.
                // i.e. for a phone call
                Logger.d(LOG_TAG, "Audio focus change: TRANSIENT")
                wasPlaying = player.isPlaying()
                player.pause()
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Stop playback, because you lost the Audio Focus.
                // i.e. the user started some other playback app
                // Remember to unregister your controls/buttons here.
                // And release the kra — Audio Focus!
                // You’re done.
                Logger.d(LOG_TAG, "Audio focus change: LOSS")
                wasPlaying = player.isPlaying()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    audioFocusRequest?.let { request ->
                        audioManager?.abandonAudioFocusRequest(request)
                    }
                } else {
                    audioManager?.abandonAudioFocus(this)
                }
                player.pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Lower the volume, because something else is also
                // playing audio over you.
                // i.e. for notifications or navigation directions
                // Depending on your audio playback, you may prefer to
                // pause playback here instead. You do you.
                Logger.d(LOG_TAG, "Audio focus change: CAN_DUCK")
                wasPlaying = player.isPlaying()
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                // Resume playback, because you hold the Audio Focus
                // again!
                // i.e. the phone call ended or the nav directions
                // are finished
                // If you implement ducking and lower the volume, be
                // sure to return it to normal here, as well.
                Logger.d(LOG_TAG, "Audio focus change: GAIN")
                if (wasPlaying) { // ok, the player was playing, we're good to resume playback
                    player.start()
                }
            }
        }
    }

    // requests audio focus from the audio manager
    fun requestAudioFocus(): Boolean {
        val manager = audioManager
        if (manager != null) {
            Logger.d(LOG_TAG, "Requesting audio focus")
            val focusRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val attrs = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setAudioAttributes(attrs)
                        .setAcceptsDelayedFocusGain(true)
                        .setOnAudioFocusChangeListener(this) // Need to implement listener
                        .build()
                audioFocusRequest = request
                manager.requestAudioFocus(request)
            } else {
                manager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
            }

            return when(focusRequest) {
                AudioManager.AUDIOFOCUS_REQUEST_FAILED -> { // don’t start playback
                    Logger.d(LOG_TAG, "Audio focus NOT granted")
                    false
                }
                AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> { // actually start playback
                    Logger.d(LOG_TAG, "Audio focus granted")
                    true
                }
                else -> true
            }
        } else {
            Logger.d(LOG_TAG, "Cannot request audio focus: audio manager is null")
            // audio manager is null, we can't request audio focus, it's supposed to be granted
            return true
        }
    }

}