package com.frolo.muse.engine


interface AudioFxApplicable : AudioFx {
    /**
     * Applies all audio effects to the given audio session id
     */
    fun apply(audioSessionId: Int)
}