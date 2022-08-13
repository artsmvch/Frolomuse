package com.frolo.audiofx2.impl

import android.content.Context
import android.media.audiofx.AudioEffect
import com.frolo.audiofx2.*

class AudioFx2Impl constructor(
    private val context: Context,
    private val errorHandler: AudioEffect2ErrorHandler
): AudioFx2 {
    private val hasEqualizer: Boolean
    private val hasBassBoost: Boolean
    private val hasVirtualizer: Boolean
    private val hasReverb: Boolean

    init {
        var hasEqualizer = false
        var hasBassBoost = false
        var hasVirtualizer = false
        var hasReverb = false
        try {
            val descriptors = AudioEffect.queryEffects()
            if (descriptors != null) {
                for (descriptor in descriptors) {
                    when {
                        AudioEffect.EFFECT_TYPE_EQUALIZER == descriptor.type -> {
                            hasEqualizer = true
                        }
                        AudioEffect.EFFECT_TYPE_BASS_BOOST == descriptor.type -> {
                            hasBassBoost = true
                        }
                        AudioEffect.EFFECT_TYPE_VIRTUALIZER == descriptor.type -> {
                            hasVirtualizer = true
                        }
                        AudioEffect.EFFECT_TYPE_PRESET_REVERB == descriptor.type -> {
                            hasReverb = true
                        }
                    }
                }
            }
        } catch (e: Throwable) {
        }
        this.hasEqualizer = hasEqualizer
        this.hasBassBoost = hasBassBoost
        this.hasVirtualizer = hasVirtualizer
        this.hasReverb = hasReverb
    }

    override val equalizer: Equalizer? = kotlin.run {
        if (hasEqualizer) {
            EqualizerImpl(context, errorHandler)
        } else {
            null
        }
    }
    override val bassBoost: BassBoost? = null
    override val virtualizer: Virtualizer? = null
    override val reverb: Reverb? = null
}