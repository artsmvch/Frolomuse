package com.frolo.muse.interactor.player

import com.frolo.muse.common.AudioSourceQueue
import com.frolo.muse.common.prepareByTarget
import com.frolo.muse.common.toAudioSource
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
            .doOnSuccess { song ->
                val queue = AudioSourceQueue(song)
                player.prepareByTarget(queue, song.toAudioSource(), true)
            }
            .ignoreElement()
            .observeOn(schedulerProvider.main())
            .doOnComplete { navigator.openPlayer() }
    }

}