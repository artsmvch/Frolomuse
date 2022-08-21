package com.frolo.audiofx2.impl

import android.content.Context
import androidx.annotation.StringRes
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
    private val context: Context,
    internal val index: Int,
    val keyName: String
) : EqualizerPreset.Native {
    override val name: String get() = resolveLocalizedName()
    override val isDeletable: Boolean = false

    private fun resolveLocalizedName(): String {
        @StringRes
        val nameResId = when(keyName.toLowerCase()) {
            "normal" ->         R.string.preset_normal
            "rock" ->           R.string.preset_rock
            "heavy metal" ->    R.string.preset_heavy_metal
            "classical" ->      R.string.preset_classical
            "folk" ->           R.string.preset_folk
            "flat" ->           R.string.preset_flat
            "dance" ->          R.string.preset_dance
            "hip hop" ->        R.string.preset_hip_hop
            "jazz" ->           R.string.preset_jazz
            "pop" ->            R.string.preset_pop
            else ->             0
        }
        if (nameResId == 0) {
            return keyName
        }
        return context.getString(nameResId)
    }

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