package com.frolo.player

import android.media.MediaPlayer

interface MediaPlayerHook {
    fun attachAudioEffects(mediaPlayer: MediaPlayer)
    fun releaseAudioEffects()
}