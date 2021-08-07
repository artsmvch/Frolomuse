package com.frolo.muse.interactor.media

import com.frolo.muse.Features
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.logFailedToTransferPlaylists
import com.frolo.muse.logger.logPlaylistsTransferred
import com.frolo.muse.permission.PermissionChecker
import com.frolo.muse.repository.PlaylistRepository
import com.frolo.muse.repository.PlaylistTransferPreferences
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject


/**
 * Transfers playlists from the shared storage to the app storage.
 * The idea is to transfer them once as soon as possible and then
 * forget this nightmare shared storage.
 */
class TransferPlaylistsUseCase @Inject constructor(
    private val permissionChecker: PermissionChecker,
    private val playlistTransferPreferences: PlaylistTransferPreferences,
    private val playlistRepository: PlaylistRepository,
    private val schedulerProvider: SchedulerProvider,
    private val eventLogger: EventLogger
) {

    private fun currentTimeMillis(): Long = System.currentTimeMillis()

    fun transferPlaylistsIfNecessary(): Completable {
        if (!Features.isAppPlaylistStorageFeatureAvailable()) {
            return Completable.complete()
        }

        return playlistTransferPreferences.requestTransfer()
            .flatMapCompletable { transfer ->
                if (transfer) {
                    actualTransferPlaylists()
                } else {
                    Completable.complete()
                }
            }
            .observeOn(schedulerProvider.main())
    }

    private fun actualTransferPlaylists(): Completable {
        if (!permissionChecker.isQueryMediaContentPermissionGranted) {
            // Maybe next time
            return Completable.error(SecurityException(
                    "Permission required to query playlists from the shared storage"))
        }

        val startTimeMillis = AtomicLong()
        return playlistRepository.transferFromSharedStorage()
            .andThen(playlistTransferPreferences.completeTransfer())
            .doOnSubscribe { startTimeMillis.set(currentTimeMillis()) }
            .doOnComplete {
                val timePassed: Long = currentTimeMillis() - startTimeMillis.get()
                eventLogger.logPlaylistsTransferred(timePassed)
            }
            .doOnError { err -> eventLogger.logFailedToTransferPlaylists(err) }
    }

}