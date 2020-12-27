package com.frolo.muse.interactor.player

import com.frolo.muse.common.prepareByTarget
import com.frolo.muse.common.title
import com.frolo.muse.common.toAudioSource
import com.frolo.muse.engine.AudioSourceQueue
import com.frolo.muse.engine.Player
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.repository.SongRepository
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable
import javax.inject.Inject


class OpenAudioSourceUseCase @Inject constructor(
    private val songRepository: SongRepository,
    private val schedulerProvider: SchedulerProvider,
    private val navigator: Navigator
) {

    fun openAudioSource(player: Player, source: String): Completable {
        return songRepository.getSong(source)
            .subscribeOn(schedulerProvider.worker())
            .map { song -> song.toAudioSource() }
            .doOnSuccess { audioSource ->
                val queue = AudioSourceQueue.create(
                        AudioSourceQueue.SINGLE, audioSource.id, audioSource.title, listOf(audioSource))
                player.prepareByTarget(queue, audioSource, true)
            }
            .ignoreElement()
            .observeOn(schedulerProvider.main())
            .doOnComplete { navigator.openPlayer() }
    }

}