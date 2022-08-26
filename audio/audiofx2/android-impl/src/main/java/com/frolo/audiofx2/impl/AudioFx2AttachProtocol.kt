package com.frolo.audiofx2.impl

interface AudioFx2AttachProtocol {
    fun attachTo(target: AudioFx2AttachTarget)
    fun release()
}