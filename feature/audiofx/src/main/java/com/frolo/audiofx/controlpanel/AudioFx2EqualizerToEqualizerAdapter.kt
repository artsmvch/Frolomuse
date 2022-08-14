package com.frolo.audiofx.controlpanel

import com.frolo.equalizerview.IEqualizer


internal class AudioFx2EqualizerToEqualizerAdapter(
    private val equalizer: com.frolo.audiofx2.Equalizer
): IEqualizer {

    override val numberOfBands: Int
        get() = equalizer.numberOfBands

    override val minBandLevelRange: Int
        get() = equalizer.bandLevelRange.minValue

    override val maxBandLevelRange: Int
        get() = equalizer.bandLevelRange.maxValue

    override fun getBandLevel(band: Short): Short {
        return equalizer.getBandLevel(band.toInt()).toShort()
    }

    override fun setBandLevel(band: Short, level: Short) {
        equalizer.setBandLevel(band.toInt(), level.toInt())
    }

    override fun getBandFreqRange(band: Short): IntArray {
        return equalizer.getFreqRange(band.toInt()).let {
            intArrayOf(it.minValue, it.maxValue)
        }
    }

    override fun registerObserver(observer: IEqualizer.Observer) {
        equalizer.addOnBandLevelChangeListener(AudioFxObserverWrapper(observer))
    }

    override fun unregisterObserver(observer: IEqualizer.Observer) {
        equalizer.removeOnBandLevelChangeListener(AudioFxObserverWrapper(observer))
    }

    private class AudioFxObserverWrapper(
        private val equalizerObserver: IEqualizer.Observer
    ): com.frolo.audiofx2.Equalizer.OnBandLevelChangeListener {

        override fun onBandLevelChange(equalizer: com.frolo.audiofx2.Equalizer, band: Int, level: Int) {
            equalizerObserver.onBandLevelChanged(band.toShort(), level.toShort())
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