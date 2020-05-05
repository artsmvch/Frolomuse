package com.frolo.muse.interactor.media.shortcut

import com.frolo.muse.engine.Player
import com.frolo.muse.engine.SongQueueFactory
import com.frolo.muse.model.media.Media
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.repository.*
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable
import javax.inject.Inject


class NavigateToMediaUseCase @Inject constructor(
    private val songRepository: SongRepository,
    private val albumRepository: AlbumRepository,
    private val artistRepository: ArtistRepository,
    private val genreRepository: GenreRepository,
    private val playlistRepository: PlaylistRepository,
    private val myFileRepository: MyFileRepository,
    private val schedulerProvider: SchedulerProvider,
    private val navigator: Navigator,
    private val songQueueFactory: SongQueueFactory,
    private val player: Player
) {

    fun navigate(@Media.Kind kindOfMedia: Int, mediaId: Long): Completable = when(kindOfMedia) {

        Media.ALBUM ->
            albumRepository.getItem(mediaId)
                .subscribeOn(schedulerProvider.worker())
                .observeOn(schedulerProvider.main())
                .firstOrError()
                .doOnSuccess { navigator.openAlbum(it) }
                .ignoreElement()

        Media.ARTIST ->
            artistRepository.getItem(mediaId)
                .subscribeOn(schedulerProvider.worker())
                .observeOn(schedulerProvider.main())
                .firstOrError()
                .doOnSuccess { navigator.openArtist(it) }
                .ignoreElement()

        Media.GENRE ->
            genreRepository.getItem(mediaId)
                .subscribeOn(schedulerProvider.worker())
                .observeOn(schedulerProvider.main())
                .firstOrError()
                .doOnSuccess { navigator.openGenre(it) }
                .ignoreElement()

        Media.MY_FILE ->
            myFileRepository.getItem(mediaId)
                .subscribeOn(schedulerProvider.worker())
                .observeOn(schedulerProvider.main())
                .firstOrError()
                .doOnSuccess { navigator.openMyFile(it) }
                .ignoreElement()

        Media.PLAYLIST ->
            playlistRepository.getItem(mediaId)
                .subscribeOn(schedulerProvider.worker())
                .observeOn(schedulerProvider.main())
                .firstOrError()
                .doOnSuccess { navigator.openPlaylist(it) }
                .ignoreElement()

        Media.SONG ->
            songRepository.getItem(mediaId)
                .subscribeOn(schedulerProvider.worker())
                .observeOn(schedulerProvider.main())
                .firstOrError()
                .doOnSuccess { song ->
                    val songQueue = songQueueFactory.create(listOf(song), listOf(song))
                    player.prepare(songQueue, song, true)
                    navigator.openSong(song)
                }
                .ignoreElement()

        else -> Completable.error(IllegalArgumentException("Unknown kind of media: $kindOfMedia"))
    }

}