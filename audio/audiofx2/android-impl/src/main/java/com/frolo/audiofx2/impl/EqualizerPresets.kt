package com.frolo.audiofx2.impl

import com.frolo.audiofx2.EqualizerPreset


internal data class CustomPresetImpl(
    override val name: String
) : EqualizerPreset.Custom {
    override val isDeletable: Boolean = false

    override fun isTheSame(other: EqualizerPreset): Boolean {
        return other is CustomPresetImpl
    }
}

internal class NativePresetImpl(
    override val name: String,
    internal val index: Int,
    val keyName: String
) : EqualizerPreset.Native {
    override val isDeletable: Boolean = false

    override fun isTheSame(other: EqualizerPreset): Boolean {
        return other is NativePresetImpl && keyName == other.keyName
    }
}

internal data class SavedPresetImpl(
    override val id: Long,
    override val name: String,
    internal val bandLevels: Map<Int, Int>,
    internal val timedAdded: Long
) : EqualizerPreset.Saved {
    override val isDeletable: Boolean = true
    override val numberOfBands: Int = bandLevels.size
    override fun getBandLevel(band: Int): Int {
        return bandLevels[band]!!
    }

    override fun isTheSame(other: EqualizerPreset): Boolean {
        return other is SavedPresetImpl && id == other.id
    }
}