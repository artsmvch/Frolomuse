package com.frolo.muse.interactor.media.shortcut

import com.frolo.muse.model.media.Media
import com.frolo.muse.repository.MediaRepository
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable
import javax.inject.Inject


class CreateShortcutUseCase<E: Media> @Inject constructor(
    private val repository: MediaRepository<E>,
    private val schedulerProvider: SchedulerProvider
) {

    fun createShortcut(item: E): Completable =
        repository.createShortcut(item)
            .observeOn(schedulerProvider.main())

}