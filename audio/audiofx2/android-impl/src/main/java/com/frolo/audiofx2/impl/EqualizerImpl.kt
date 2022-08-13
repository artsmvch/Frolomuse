package com.frolo.audiofx2.impl

import android.content.Context
import androidx.annotation.GuardedBy
import com.frolo.audiofx2.AudioEffectDescriptor
import com.frolo.audiofx2.EffectValueRange
import com.frolo.audiofx2.Equalizer

internal class EqualizerImpl(
    private val context: Context,
    private val errorHandler: AudioEffect2ErrorHandler
): Equalizer {
    private val state = EqualizerState()
    private val lock = Any()
    @GuardedBy("lock")
    private val engine: android.media.audiofx.Equalizer? = null
    private val defaults = Defaults(context)

    override val descriptor: AudioEffectDescriptor =
        SimpleAudioEffectDescriptor(name = "Equalizer")

    override var isEnabled: Boolean
        get() = state.isEnabled
        set(value) {
            state.isEnabled = value
        }

    override val numberOfBands: Int get() {
        return synchronized(lock) {
            try {
                engine?.let {
                    return@synchronized it.numberOfBands.toInt()
                }
            } catch (e: Throwable) {
                errorHandler.onAudioEffectError(this, e)
            }
            return@synchronized defaults.numberOfBands
        }
    }

    override fun getBandLevel(bandIndex: Int): Int {
        return synchronized(lock) {
            try {
                engine?.let {
                    return@synchronized it.getBandLevel(bandIndex.toShort()).toInt()
                }
            } catch (e: Throwable) {
                errorHandler.onAudioEffectError(this, e)
            }
            return@synchronized defaults.zeroBandLevel
        }
    }

    override fun setBandLevel(bandIndex: Int, level: Int) {
        return synchronized(lock) {
            try {
                engine?.also {
                    it.setBandLevel(bandIndex.toShort(), level.toShort())
                }
            } catch (e: Throwable) {
                errorHandler.onAudioEffectError(this, e)
            }
        }
    }

    override fun getFreqRange(bandIndex: Int): EffectValueRange {
        return synchronized(lock) {
            try {
                engine?.let {
                    val arr = it.getBandFreqRange(bandIndex.toShort())
                    return@synchronized EffectValueRange(
                        minLevel = arr[0],
                        maxLevel = arr[1]
                    )
                }
            } catch (e: Throwable) {
                errorHandler.onAudioEffectError(this, e)
            }
            return@synchronized defaults.getDefaultBandFreqRange(bandIndex)
        }
    }
}

private class EqualizerState {
    var isEnabled: Boolean = false
}