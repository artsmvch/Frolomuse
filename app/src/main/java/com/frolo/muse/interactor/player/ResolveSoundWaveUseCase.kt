package com.frolo.muse.interactor.player

import com.frolo.muse.model.sound.SoundWave
import com.frolo.muse.repository.SoundWaveResolver
import io.reactivex.Flowable
import javax.inject.Inject


class ResolveSoundWaveUseCase @Inject constructor(
    private val soundWaveResolver: SoundWaveResolver
){

    fun resolveSoundWave(source: String): Flowable<SoundWave> =
        soundWaveResolver.resolveSoundWave(source)

}