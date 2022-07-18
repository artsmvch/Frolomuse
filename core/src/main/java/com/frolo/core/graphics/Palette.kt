package com.frolo.core.graphics


interface Palette {
    fun getSwatch(target: Target): Swatch?

    enum class Target {
        LIGHT_VIBRANT,
        VIBRANT,
        DARK_VIBRANT,
        LIGHT_MUTED,
        MUTED,
        DARK_MUTED
    }
}