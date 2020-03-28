package com.frolo.muse.ui.main.audiofx

import com.frolo.muse.engine.AudioFx
import com.frolo.muse.ui.main.audiofx.eq.DbRange
import com.frolo.muse.ui.main.audiofx.eq.EqualizerProvider


/**
 * Implementation of [EqualizerProvider] based on [AudioFx].
 */
class AudioFxEqualizerProvider constructor(
    private val audioFx: AudioFx
): EqualizerProvider {

    override fun getNumberOfBands(): Int = audioFx.getNumberOfBands().toInt()

    override fun getMinBandLevel(): Short = audioFx.getMinBandLevelRange()

    override fun getMaxBandLevel(): Short = audioFx.getMaxBandLevelRange()

    override fun getBandLevel(bandIndex: Int): Short = audioFx.getBandLevel(bandIndex.toShort())

    override fun setBandLevel(bandIndex: Int, level: Short) {
        audioFx.setBandLevel(bandIndex.toShort(), level)
    }

    override fun getDbRange(bandIndex: Int): DbRange {
        val arr = audioFx.getBandFreqRange(bandIndex.toShort())
        val min = arr.getOrNull(0) ?: 0
        val max = arr.getOrNull(1) ?: 0
        return DbRange(min, max)
    }

}