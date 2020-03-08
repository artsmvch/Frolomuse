package com.frolo.muse.interactor.player

import com.frolo.muse.model.sound.Sound
import com.frolo.muse.repository.SoundResolver
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Flowable
import javax.inject.Inject


class ResolveSoundUseCase @Inject constructor(
    private val soundResolver: SoundResolver,
    private val schedulerProvider: SchedulerProvider
){

    fun resolve(source: String): Flowable<Sound> =
        soundResolver.resolve(source)
            .subscribeOn(schedulerProvider.worker())

}