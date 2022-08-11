package com.frolo.muse.graphics

import com.frolo.core.OptionalCompat
import com.frolo.core.graphics.Palette
import com.frolo.player.AudioSource
import io.reactivex.Single


interface PaletteGenerator {
    fun generatePalette(audioSource: AudioSource): Single<OptionalCompat<Palette>>
}