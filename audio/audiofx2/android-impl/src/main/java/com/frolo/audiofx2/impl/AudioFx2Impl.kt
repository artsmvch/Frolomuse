package com.frolo.audiofx2.impl

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.media.audiofx.AudioEffect
import com.frolo.audiofx2.*

class AudioFx2Impl private constructor(
    private val context: Context,
    private val storageKey: String,
    errorHandler: AudioEffect2ErrorHandler
): AudioFx2, AudioSessionApplier {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val hasEqualizer: Boolean
    private val hasBassBoost: Boolean
    private val hasVirtualizer: Boolean
    private val hasLoudnessEnhancer: Boolean
    private val hasReverb: Boolean

    private val proxyErrorHandler = AudioEffect2ErrorHandler { effect, error ->
        val exception = AudioFx2ImplException(error)
        errorHandler.onAudioEffectError(effect, exception)
    }

    init {
        var hasEqualizer = false
        var hasBassBoost = false
        var hasVirtualizer = false
        var hasLoudnessEnhancer = false
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
                        AudioEffect.EFFECT_TYPE_LOUDNESS_ENHANCER == descriptor.type -> {
                            hasLoudnessEnhancer = true
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
        this.hasLoudnessEnhancer = hasLoudnessEnhancer
        this.hasReverb = hasReverb
    }

    private val initialEffectParams: EffectInitParams by lazy {
        EffectInitParams(
            priority = 0,
            audioSessionId = audioManager.generateAudioSessionId()
        )
    }

    private fun canUsePresetReverb(): Boolean {
        // FIXME: there is a problem with PresetReverb on some devices
        return true
    }

    private val equalizerImpl: EqualizerImpl? = createIf(hasEqualizer) {
        EqualizerImpl(context, storageKey, proxyErrorHandler, initialEffectParams)
    }
    override val equalizer: Equalizer? = equalizerImpl

    private val bassBoostImpl: BassBoostImpl? = createIf(hasBassBoost) {
        BassBoostImpl(context, storageKey, proxyErrorHandler)
    }
    override val bassBoost: BassBoost? = bassBoostImpl

    private val virtualizerImpl: VirtualizerImpl? = createIf(hasVirtualizer) {
        VirtualizerImpl(context, storageKey, proxyErrorHandler)
    }
    override val virtualizer: Virtualizer? = virtualizerImpl

    private val loudnessImpl: LoudnessImpl? = createIf(hasLoudnessEnhancer) {
        LoudnessImpl(context, storageKey, proxyErrorHandler)
    }
    override val loudness: Loudness? = loudnessImpl

    private val reverbImpl: ReverbImpl? = createIf(hasReverb && canUsePresetReverb()) {
        ReverbImpl(context, storageKey, proxyErrorHandler, initialEffectParams)
    }
    override val reverb: Reverb? = reverbImpl

    private inline fun <T> createIf(predicate: Boolean, creator: () -> T): T? {
        return if (predicate) creator.invoke() else null
    }

    override fun applyToAudioSession(audioSessionId: Int) {
        equalizerImpl?.applyToAudioSession(audioSessionId)
        bassBoostImpl?.applyToAudioSession(audioSessionId)
        virtualizerImpl?.applyToAudioSession(audioSessionId)
        loudnessImpl?.applyToAudioSession(audioSessionId)
        reverbImpl?.applyToAudioSession(audioSessionId)
    }

    override fun release() {
        equalizerImpl?.release()
        bassBoostImpl?.release()
        virtualizerImpl?.release()
        loudnessImpl?.release()
        reverbImpl?.release()
    }

    companion object {
        private const val DEFAULT_STORAGE_KEY = "application"
        private val DEFAULT_ERROR_HANDLER = AudioEffect2ErrorHandler { _, _ ->  }

        fun obtain(
            application: Application,
            errorHandler: AudioEffect2ErrorHandler = DEFAULT_ERROR_HANDLER
        ): AudioFx2Impl {
            return AudioFx2Impl(
                context = application,
                storageKey = DEFAULT_STORAGE_KEY,
                errorHandler = errorHandler
            )
        }
    }
}