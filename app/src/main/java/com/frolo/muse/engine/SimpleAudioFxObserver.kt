package com.frolo.muse.engine

import com.frolo.muse.model.preset.Preset


abstract class SimpleAudioFxObserver: AudioFxObserver {
    override fun onEnabled(audioFx: AudioFx) = Unit

    override fun onDisabled(audioFx: AudioFx) = Unit

    override fun onBandLevelChanged(audioFx: AudioFx, band: Short, level: Short) = Unit

    override fun onPresetUsed(audioFx: AudioFx, preset: Preset) = Unit

    override fun onBassStrengthChanged(audioFx: AudioFx, strength: Short) = Unit

    override fun onVirtualizerStrengthChanged(audioFx: AudioFx, strength: Short) = Unit

    override fun onPresetReverbUsed(audioFx: AudioFx, presetReverbIndex: Short) = Unit
}