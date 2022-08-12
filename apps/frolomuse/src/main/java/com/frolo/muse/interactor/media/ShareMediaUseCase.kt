package com.frolo.muse.interactor.media

import com.frolo.muse.router.AppRouter
import com.frolo.music.model.Media
import com.frolo.music.repository.MediaRepository
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable


class ShareMediaUseCase <E: Media> constructor(
    private val schedulerProvider: SchedulerProvider,
    private val repository: MediaRepository<E>,
    private val appRouter: AppRouter
) {

    fun share(item: E): Completable {
        return share(listOf(item))
    }

    fun share(items: Collection<E>): Completable {
        return repository.collectSongs(items)
            .subscribeOn(schedulerProvider.worker())
            .observeOn(schedulerProvider.main())
            .doOnSuccess { songs ->
                appRouter.shareSongs(songs)
            }
            .ignoreElement()
    }

}