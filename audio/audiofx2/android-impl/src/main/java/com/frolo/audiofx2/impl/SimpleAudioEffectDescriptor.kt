package com.frolo.audiofx2.impl

import com.frolo.audiofx2.AudioEffect2Warning
import com.frolo.audiofx2.AudioEffectDescriptor

internal data class SimpleAudioEffectDescriptor(
    override val name: String,
    override val description: String? = null,
    override val warnings: List<AudioEffect2Warning> = emptyList()
) : AudioEffectDescriptor