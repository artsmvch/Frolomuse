package com.frolo.muse.interactor.media

import com.frolo.muse.engine.Player
import com.frolo.muse.engine.SongQueueFactory
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.model.media.*
import com.frolo.muse.repository.GenericMediaRepository
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.Function


class ClickMediaUseCase <E: Media> constructor(
        private val schedulerProvider: SchedulerProvider,
        private val player: Player,
        private val repository: GenericMediaRepository,
        private val navigator: Navigator,
        private val songQueueFactory: SongQueueFactory
) {

    private fun processPlay(target: Song, songs: List<Song>, toggleIfSameSong: Boolean) {
        val currentSong = player.getCurrent()
        if (toggleIfSameSong && currentSong == target) {
            // if we've chosen the same song that is currently being played then toggle the playback
            player.toggle()
        } else {
            // otherwise, create new song queue and start playing it
            val songQueue = songQueueFactory.create(listOf(target), songs)
            player.prepare(songQueue, target, true)
        }
    }

    fun click(item: E, fromCollection: Collection<E>): Completable {
        return when(item.kind) {
            Media.SONG -> {
                return Single.fromCallable { fromCollection.filterIsInstance<Song>() }
                        .subscribeOn(schedulerProvider.worker())
                        .doOnSuccess { songs ->
                            processPlay(item as Song, songs, true)
                        }
                        .ignoreElement()
            }
            Media.ALBUM -> {
                Completable.complete()
                        .doOnComplete {
                            navigator.openAlbum(item as Album)
                        }
            }
            Media.ARTIST -> {
                Completable.complete()
                        .doOnComplete {
                            navigator.openArtist(item as Artist)
                        }
            }
            Media.GENRE -> {
                Completable.complete()
                        .doOnComplete {
                            navigator.openGenre(item as Genre)
                        }
            }
            Media.PLAYLIST -> {
                Completable.complete()
                        .doOnComplete {
                            navigator.openPlaylist(item as Playlist)
                        }
            }
            Media.MY_FILE -> {
                val myFile = item as MyFile
                when {
                    myFile.isDirectory -> Completable.complete()
                            .doOnComplete {
                                navigator.openMyFile(item as MyFile)
                            }

                    myFile.isSongFile -> repository.collectSongs(myFile)
                            .subscribeOn(schedulerProvider.worker())
                            // as the item is song file itself then we must get a collection of just 1 item
                            .map { songs -> songs.first() }
                            .flatMap { targetSong ->
                                val sources = fromCollection.filter { it is MyFile && it.isSongFile }
                                        .map { myFile ->
                                            repository.collectSongs(myFile).onErrorReturnItem(emptyList())
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
                            .doOnSuccess { pair ->
                                processPlay(pair.first, pair.second, true)
                            }
                            .ignoreElement()

                    else -> Completable.complete()
                }
            }
            else -> Completable.error(
                    UnknownMediaException(item))
        }
    }

}