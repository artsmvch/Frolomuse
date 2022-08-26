package com.frolo.audiofx2.impl

import android.media.MediaPlayer

class AudioFx2AttachTarget(
    val priority: Int,
    val sessionId: Int,
    val mediaPlayer: MediaPlayer?
)