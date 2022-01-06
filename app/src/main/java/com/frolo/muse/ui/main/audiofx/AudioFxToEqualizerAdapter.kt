package com.frolo.muse.ui.main.audiofx

import com.frolo.audiofx.AudioFx
import com.frolo.audiofx.SimpleAudioFxObserver
import com.frolo.equalizerview.Equalizer


internal class AudioFxToEqualizerAdapter(
    private val audioFx: AudioFx
): Equalizer {

    override val numberOfBands: Int
        get() = audioFx.numberOfBands.toInt()

    override val minBandLevelRange: Int
        get() = audioFx.minBandLevelRange.toInt()

    override val maxBandLevelRange: Int
        get() = audioFx.maxBandLevelRange.toInt()

    override fun getBandLevel(band: Short): Short {
        return audioFx.getBandLevel(band)
    }

    override fun setBandLevel(band: Short, level: Short) {
        audioFx.setBandLevel(band, level)
    }

    override fun getBandFreqRange(band: Short): IntArray {
        return audioFx.getBandFreqRange(band)
    }

    override fun registerObserver(observer: Equalizer.Observer) {
        audioFx.registerObserver(AudioFxObserverImpl(observer))
    }

    override fun unregisterObserver(observer: Equalizer.Observer) {
        audioFx.unregisterObserver(AudioFxObserverImpl(observer))
    }

    private class AudioFxObserverImpl(
        private val equalizerObserver: Equalizer.Observer
    ): SimpleAudioFxObserver() {

        override fun onBandLevelChanged(audioFx: AudioFx?, band: Short, level: Short) {
            equalizerObserver.onBandLevelChanged(band, level)
        }

        override fun hashCode(): Int {
            return equalizerObserver.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            if (other is AudioFxObserverImpl) {
                return equalizerObserver == other.equalizerObserver
            }
            return false
        }
    }
}