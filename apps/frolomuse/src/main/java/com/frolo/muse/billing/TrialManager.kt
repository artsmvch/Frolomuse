package com.frolo.muse.billing

import io.reactivex.Completable
import io.reactivex.Flowable

interface TrialManager {
    fun sync(): Completable

    fun getTrialStatus(): Flowable<TrialStatus>

    fun activateTrialVersion(): Completable

    /**
     * Resets trial activation. NOTE: should be used only for testing.
     */
    fun resetTrial(): Completable
}