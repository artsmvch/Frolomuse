package com.frolo.muse.interactor.firebase

import com.frolo.muse.repository.FirebasePreferences
import com.frolo.muse.repository.FirebaseRemoteRepository
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable
import javax.inject.Inject


class SyncFirebaseMessagingTokenUseCase @Inject constructor(
    private val firebaseRemoteRepository: FirebaseRemoteRepository,
    private val firebasePreferences: FirebasePreferences,
    private val schedulerProvider: SchedulerProvider
) {

    fun sync(): Completable {
        return firebaseRemoteRepository.getMessagingToken()
            .subscribeOn(schedulerProvider.worker())
            .flatMapCompletable { token ->
                firebasePreferences.setMessagingToken(token)
            }
    }

}