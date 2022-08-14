package com.frolo.audiofx2.impl

import com.frolo.audiofx2.EqualizerPreset


internal data class NativePresetImpl(
    override val name: String,
    internal val index: Int,
    internal val keyName: String
) : EqualizerPreset.Native {
    override val isDeletable: Boolean = false
}

internal data class CustomPresetImpl(
    override val id: Long,
    override val name: String,
    internal val bandLevels: Map<Int, Int>,
    internal val timedAdded: Long
) : EqualizerPreset.Custom {
    override val isDeletable: Boolean = true
    override val numberOfBands: Int = bandLevels.size
    override fun getBandLevel(band: Int): Int {
        return bandLevels[band]!!
    }
}