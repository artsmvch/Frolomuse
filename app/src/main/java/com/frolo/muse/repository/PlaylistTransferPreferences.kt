package com.frolo.muse.repository

import io.reactivex.Completable
import io.reactivex.Single


interface PlaylistTransferPreferences {
    /**
     * Requests transfer of playlists from the shared storage to the app storage.
     * Should return true only if [completeTransfer] hasn't been completed earlier.
     * After successful transfer of playlists [completeTransfer] must be called and completed.
     */
    fun requestTransfer(): Single<Boolean>

    /**
     * Marks transfer of playlists from the shared storage as complete.
     * Can be called even if there was no transfer and it is not needed.
     */
    fun completeTransfer(): Completable
}