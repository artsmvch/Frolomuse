package com.frolo.muse.interactor.player

import com.frolo.muse.common.blockingCreateAudioSourceQueue
import com.frolo.player.prepareByTarget
import com.frolo.muse.common.toAudioSource
import com.frolo.player.Player
import com.frolo.muse.router.AppRouter
import com.frolo.music.repository.SongRepository
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable
import javax.inject.Inject


class OpenAudioSourceUseCase @Inject constructor(
    private val songRepository: SongRepository,
    private val schedulerProvider: SchedulerProvider,
    private val appRouter: AppRouter
) {

    fun openAudioSource(player: Player, source: String): Completable {
        return songRepository.getSong(source)
            .doOnSuccess { song ->
                val queue = blockingCreateAudioSourceQueue(song)
                player.prepareByTarget(queue, song.toAudioSource(), true)
            }
            .ignoreElement()
            .observeOn(schedulerProvider.main())
            .doOnComplete { appRouter.openPlayer() }
    }

}