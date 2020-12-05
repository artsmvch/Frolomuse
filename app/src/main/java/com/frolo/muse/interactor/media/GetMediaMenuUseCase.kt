package com.frolo.muse.interactor.media

import com.frolo.muse.Features
import com.frolo.muse.common.toAudioSource
import com.frolo.muse.engine.Player
import com.frolo.muse.model.media.*
import com.frolo.muse.model.menu.ContextualMenu
import com.frolo.muse.model.menu.OptionsMenu
import com.frolo.muse.repository.MediaRepository
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.BiFunction


class GetMediaMenuUseCase<E: Media> constructor(
    private val schedulerProvider: SchedulerProvider,
    private val repository: MediaRepository<E>,
    private val player: Player
) {

    fun getOptionsMenu(item: E): Single<OptionsMenu<E>> {
        // First check if the item can be favourite and if so then check if it is favourite
        val favouriteOptionOperator = if (item is Song) {
            repository.isFavourite(item)
                .firstOrError()
                .map { isFavourite -> true to isFavourite }
        } else {
            Single.just(false to false)
        }

        val zipper: BiFunction<Pair<Boolean, Boolean>, Boolean, OptionsMenu<E>> =
            BiFunction { favouriteOption, isShortcutSupported ->
                val editOptionAvailable = item is Song || item is Playlist
                        || (item is Album && Features.isAlbumEditorFeatureAvailable())
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
                    viewAlbumOptionAvailable = false,//item is Song,
                    viewArtistOptionAvailable = false,//item is Song || item is Album
                    setAsDefaultOptionAvailable = item is MyFile && item.isDirectory,
                    addToHiddenOptionAvailable = item is MyFile,
                    scanFilesOptionAvailable = item is MyFile && item.isDirectory,
                    shortcutOptionAvailable = isShortcutSupported,
                    removeFromCurrentQueue = item is Song && player.getCurrentQueue()?.contains(item.toAudioSource()) ?: false
                )
            }

        return favouriteOptionOperator
            .zipWith(repository.isShortcutSupported(item), zipper)
            .subscribeOn(schedulerProvider.worker())
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