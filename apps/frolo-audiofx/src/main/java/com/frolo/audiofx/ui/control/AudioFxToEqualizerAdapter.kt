package com.frolo.audiofx.ui.control

import com.frolo.audiofx.AudioFx
import com.frolo.audiofx.SimpleAudioFxObserver
import com.frolo.equalizerview.IEqualizer


internal class AudioFxToEqualizerAdapter(
    private val audioFx: AudioFx
): IEqualizer {

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

    override fun registerObserver(observer: IEqualizer.Observer) {
        audioFx.registerObserver(AudioFxObserverWrapper(observer))
    }

    override fun unregisterObserver(observer: IEqualizer.Observer) {
        audioFx.unregisterObserver(AudioFxObserverWrapper(observer))
    }

    private class AudioFxObserverWrapper(
        private val equalizerObserver: IEqualizer.Observer
    ): SimpleAudioFxObserver() {

        override fun onBandLevelChanged(audioFx: AudioFx?, band: Short, level: Short) {
            equalizerObserver.onBandLevelChanged(band, level)
        }

        override fun hashCode(): Int {
            return equalizerObserver.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            if (other is AudioFxObserverWrapper) {
                return equalizerObserver == other.equalizerObserver
            }
            return false
        }
    }
}