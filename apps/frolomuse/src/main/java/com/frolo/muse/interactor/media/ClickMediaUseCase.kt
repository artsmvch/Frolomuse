package com.frolo.muse.interactor.media

import com.frolo.muse.common.createAudioSourceQueue
import com.frolo.player.Player
import com.frolo.player.prepareByTarget
import com.frolo.muse.common.toAudioSource
import com.frolo.muse.router.AppRouter
import com.frolo.music.repository.GenericMediaRepository
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.music.model.*
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function


class ClickMediaUseCase <E: Media> constructor(
    private val schedulerProvider: SchedulerProvider,
    private val player: Player,
    private val genericMediaRepository: GenericMediaRepository,
    private val appRouter: AppRouter
) {

    private fun playOrToggle(
        target: Song,
        songs: List<Song>,
        associatedMediaItem: Media?
    ): Completable {
        val currentAudioSource = player.getCurrent()
        return if (currentAudioSource?.getURI() == target.getMediaId().getURI()) {
            Completable.fromAction { player.toggle() }
        } else {
            createAudioSourceQueue(songs, associatedMediaItem)
                .doOnSuccess { queue ->
                    player.prepareByTarget(queue, target.toAudioSource(), true)
                }
                .ignoreElement()
        }
    }

    fun click(item: E, fromCollection: Collection<E>, associatedMediaItem: Media? = null): Completable = when(item.getMediaId().getKind()) {
        Media.SONG -> {
            Single.fromCallable { fromCollection.filterIsInstance<Song>() }
                .subscribeOn(schedulerProvider.computation())
                .flatMapCompletable { songs ->
                    playOrToggle(item as Song, songs, associatedMediaItem)
                }
        }

        Media.ALBUM -> {
            Single.fromCallable { item as Album }
                .observeOn(schedulerProvider.main())
                .doOnSuccess { appRouter.openAlbum(it) }
                .ignoreElement()
        }

        Media.ARTIST -> {
            Single.fromCallable { item as Artist }
                .observeOn(schedulerProvider.main())
                .doOnSuccess { appRouter.openArtist(it) }
                .ignoreElement()
        }

        Media.GENRE -> {
            Single.fromCallable { item as Genre }
                .observeOn(schedulerProvider.main())
                .doOnSuccess { appRouter.openGenre(it) }
                .ignoreElement()
        }

        Media.PLAYLIST -> {
            Single.fromCallable { item as Playlist }
                .observeOn(schedulerProvider.main())
                .doOnSuccess { appRouter.openPlaylist(it) }
                .ignoreElement()
        }

        Media.MY_FILE -> {
            Single.fromCallable { item as MyFile }
                .flatMapCompletable { myFile -> when {
                    myFile.isDirectory -> Completable.complete()
                        .observeOn(schedulerProvider.main())
                        .doOnComplete { appRouter.openMyFile(myFile) }

                    myFile.isSongFile -> genericMediaRepository.collectSongs(myFile)
                        .subscribeOn(schedulerProvider.worker())
                        // Since the item is a song file itself then we create a collection of just 1 item
                        .map { songs -> songs.first() }
                        .flatMap { targetSong ->
                            val sources = fromCollection
                                .filter { it is MyFile && it.isSongFile }
                                .map { myFile ->
                                    genericMediaRepository.collectSongs(myFile).onErrorReturnItem(emptyList())
                                }

                            Single.zip(
                                sources,
                                Function<Array<*>, List<Song>> { arr ->
                                    arr.filter { it is List<*> && it.size == 1 }
                                            .map { (it as List<*>).first() as Song }
                                })
                                .onErrorResumeNext(Single.just(listOf(targetSong)))
                                .map { songs -> targetSong to songs }
                        }
                        .flatMapCompletable { pair ->
                            playOrToggle(pair.first, pair.second, associatedMediaItem)
                        }

                    else -> Completable.complete()
                } }
        }

        Media.MEDIA_FILE -> {
            val source1 = Single.fromCallable { item as MediaFile }
                .flatMap { genericMediaRepository.collectSongs(it) }
                .map { it.first() }
                .subscribeOn(schedulerProvider.worker())
            val source2 = Single.fromCallable { fromCollection.map { it as MediaFile } }
                .flatMap { genericMediaRepository.collectSongs(it) }
                .subscribeOn(schedulerProvider.worker())
            val zipper = BiFunction<Song, List<Song>, Pair<Song, List<Song>>> { song, songList ->
                song to songList
            }
            Single.zip(source1, source2, zipper)
                .flatMapCompletable { pair ->
                    playOrToggle(pair.first, pair.second, associatedMediaItem)
                }
        }

        else -> Completable.error(UnknownMediaException(item))
    }

}