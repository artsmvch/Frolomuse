package com.frolo.muse.interactor.feature

import com.frolo.muse.repository.LyricsRemoteRepository
import com.frolo.muse.repository.RemoteConfigRepository
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.Single
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject


class FeaturesUseCase @Inject constructor(
    private val remoteConfigRepository: RemoteConfigRepository,
    private val lyricsRemoteRepository: LyricsRemoteRepository,
    private val schedulerProvider: SchedulerProvider
) {

    private val lyricsViewerAvailableRef = AtomicBoolean(true)

    fun sync(): Completable {
        return remoteConfigRepository.isLyricsViewerEnabled()
            .subscribeOn(schedulerProvider.worker())
            .doOnSuccess { isEnabled -> lyricsViewerAvailableRef.set(isEnabled) }
            .ignoreElement()
    }

    fun isPurchaseFeatureEnabled(): Single<Boolean> {
        return remoteConfigRepository.isPurchaseFeatureEnabled()
    }

    fun isLyricsViewerEnabled(): Single<Boolean> {
        return Single.fromCallable { lyricsViewerAvailableRef.get() }
    }

}