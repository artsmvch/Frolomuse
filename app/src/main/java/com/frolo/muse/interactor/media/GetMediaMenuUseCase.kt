package com.frolo.muse.interactor.media

import com.frolo.muse.Features
import com.frolo.muse.common.toAudioSource
import com.frolo.player.Player
import com.frolo.muse.model.menu.ContextualMenu
import com.frolo.muse.model.menu.OptionsMenu
import com.frolo.muse.repository.MediaRepository
import com.frolo.muse.repository.RemoteConfigRepository
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.music.model.*
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.Function
import java.util.concurrent.TimeUnit


class GetMediaMenuUseCase<E: Media> constructor(
    private val schedulerProvider: SchedulerProvider,
    private val mediaRepository: MediaRepository<E>,
    private val remoteConfigRepository: RemoteConfigRepository,
    private val player: Player
) {

    fun getOptionsMenu(item: E): Single<OptionsMenu<E>> {
        // First check if the item can be favourite and if so then check if it is favourite
        val favouriteOptionSource = if (item is Song) {
            mediaRepository.isFavourite(item)
                .firstOrError()
                .map { isFavourite -> true to isFavourite }
                .subscribeOn(schedulerProvider.worker())
                .onErrorReturn { true to false }
        } else {
            Single.just(false to false)
        }

        val isLyricsSupportedSource = if (item is Song) {
            remoteConfigRepository.isLyricsViewerEnabled()
                .timeout(500L, TimeUnit.MILLISECONDS, Single.just(false))
                .subscribeOn(schedulerProvider.worker())
                .onErrorReturn { false }
        } else {
            Single.just(false)
        }

        val isShortcutSupportedSource = mediaRepository.isShortcutSupported(item)
                .onErrorReturn { false }

        val zipper: Function<Array<*>, OptionsMenu<E>> =
            Function { arr ->
                val favouriteOption = arr[0] as Pair<Boolean, Boolean>
                val isLyricsSupported = arr[1] as Boolean
                val isShortcutSupported = arr[2] as Boolean

                val editOptionAvailable = item is Song || item is Playlist
                        || (item is Album && Features.isAlbumEditorFeatureAvailable())
                val removeFromQueueOptionAvailable = item is Song
                        && player.getCurrentQueue()?.contains(item.toAudioSource()) ?: false
                OptionsMenu(
                    item = item,
                    favouriteOptionAvailable = favouriteOption.first,
                    isFavourite = favouriteOption.second,
                    shareOptionAvailable = true,
                    deleteOptionAvailable = true,
                    playOptionAvailable = true,
                    playNextOptionAvailable = true,
                    addToPlaylistOptionAvailable = item !is Playlist, // you can add everything to the playlist except the playlists themselves
                    editOptionAvailable = editOptionAvailable,
                    addToQueueOptionAvailable = true,
                    viewLyricsOptionAvailable = isLyricsSupported,
                    viewAlbumOptionAvailable = false,//item is Song,
                    viewArtistOptionAvailable = false,//item is Song || item is Album
                    setAsDefaultOptionAvailable = item is MyFile && item.isDirectory,
                    addToHiddenOptionAvailable = item is MyFile,
                    scanFilesOptionAvailable = item is MyFile && item.isDirectory,
                    shortcutOptionAvailable = isShortcutSupported,
                    removeFromQueueOptionAvailable = removeFromQueueOptionAvailable
                )
            }

        return Single.zip(listOf(favouriteOptionSource, isLyricsSupportedSource, isShortcutSupportedSource), zipper)
                .observeOn(schedulerProvider.main())
    }

    fun getContextualMenu(initiator: E): Single<ContextualMenu<E>> {
        return Single.just(
            ContextualMenu(
                targetItem = initiator,
                selectAllOptionAvailable = true,
                playOptionAvailable = true,
                playNextOptionAvailable = true,
                addToQueueOptionAvailable = true,
                deleteOptionAvailable = true,
                shareOptionAvailable = true,
                addToPlaylistOptionAvailable = true,
                hideOptionAvailable = initiator is MyFile,
                scanFilesOptionAvailable = initiator is MyFile
            )
        )
    }

    fun removeFromCurrentQueue(item: E): Completable = Completable.fromAction {
        if (item is Song) {
            val audioSource = item.toAudioSource()
            player.removeAll(listOf(audioSource))
        }
    }.subscribeOn(schedulerProvider.worker())

}