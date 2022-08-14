package com.frolo.audiofx2.impl

import android.app.Application
import android.content.Context
import android.media.audiofx.AudioEffect
import com.frolo.audiofx2.*

class AudioFx2Impl private constructor(
    private val context: Context,
    private val storageKey: String,
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

    private val equalizerImpl: EqualizerImpl? = kotlin.run {
        if (hasEqualizer) {
            EqualizerImpl(context, storageKey, errorHandler)
        } else {
            null
        }
    }
    override val equalizer: Equalizer? = equalizerImpl

    override val bassBoost: BassBoost? = null
    override val virtualizer: Virtualizer? = null
    override val reverb: Reverb? = null

    fun applyToAudioSession(audioSessionId: Int) {
        equalizerImpl?.applyToAudioSession(audioSessionId)
    }

    companion object {
        private const val DEFAULT_KEY = "application"
        private val DEFAULT_ERROR_HANDLER = AudioEffect2ErrorHandler { _, _ ->  }

        fun obtain(
            application: Application,
            errorHandler: AudioEffect2ErrorHandler = DEFAULT_ERROR_HANDLER
        ): AudioFx2Impl {
            return AudioFx2Impl(
                context = application,
                storageKey = DEFAULT_KEY,
                errorHandler = errorHandler
            )
        }
    }
}