package com.frolo.player

import android.media.MediaPlayer
import com.frolo.audiofx.AudioFx
import com.frolo.audiofx.AudioFxStub
import com.frolo.audiofx.applicable.AudioFxApplicable


internal object AudioFxApplicableStub :
    AudioFx by AudioFxStub,
    AudioFxApplicable {

    override fun apply(engine: MediaPlayer) = Unit
    override fun release() = Unit

}