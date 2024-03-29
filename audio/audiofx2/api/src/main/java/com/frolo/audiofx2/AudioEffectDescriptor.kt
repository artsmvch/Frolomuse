package com.frolo.audiofx2

interface AudioEffectDescriptor {
    val name: String
    val description: String?
    val warnings: List<AudioEffect2Warning>
}