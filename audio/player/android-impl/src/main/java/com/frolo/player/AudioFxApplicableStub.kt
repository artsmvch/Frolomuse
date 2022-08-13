package com.frolo.player

import android.media.MediaPlayer
import com.frolo.audiofx.AudioFx
import com.frolo.audiofx.AudioFxStub
import com.frolo.audiofx.applicable.AudioFxApplicable


internal object AudioFxApplicableStub :
    AudioFx by AudioFxStub,
    AudioFxApplicable {

    override fun applyTo(audioSessionId: Int) = Unit
    override fun applyTo(engine: MediaPlayer) = Unit
    override fun release() = Unit

}