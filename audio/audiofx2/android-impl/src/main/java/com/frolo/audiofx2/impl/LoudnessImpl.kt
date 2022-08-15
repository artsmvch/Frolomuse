package com.frolo.audiofx2.impl

import android.content.Context
import android.media.audiofx.LoudnessEnhancer
import com.frolo.audiofx2.AudioEffectDescriptor
import com.frolo.audiofx2.Loudness
import com.frolo.audiofx2.ValueRange

internal class LoudnessImpl(
    context: Context,
    storageKey: String,
    errorHandler: AudioEffect2ErrorHandler
): SimpleAudioEffect2Impl<android.media.audiofx.LoudnessEnhancer>(
    context = context,
    storageKey = storageKey,
    errorHandler = errorHandler
), Loudness {
    override val effectKey: String = "loudness"

    override val valueRange: ValueRange = ValueRange(minValue = 0, maxValue = 1000)
    override val descriptor: AudioEffectDescriptor
        get() = SimpleAudioEffectDescriptor(name = "Loudness")

    override fun getStrengthFrom(effect: LoudnessEnhancer): Int {
        return effect.targetGain.toInt()
    }

    override fun setStrengthTo(effect: LoudnessEnhancer, value: Int) {
        effect.setTargetGain(value)
    }

    override fun instantiateEngine(priority: Int, audioSessionId: Int): LoudnessEnhancer {
        return LoudnessEnhancer(audioSessionId)
    }
}