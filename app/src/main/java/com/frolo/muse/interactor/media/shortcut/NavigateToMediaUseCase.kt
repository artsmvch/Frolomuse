package com.frolo.muse.interactor.media.shortcut

import com.frolo.muse.common.AudioSourceQueue
import com.frolo.player.Player
import com.frolo.player.prepareByTarget
import com.frolo.muse.common.toAudioSource
import com.frolo.music.model.Media
import com.frolo.muse.router.AppRouter
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.music.repository.*
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
    private val appRouter: AppRouter,
    private val player: Player
) {

    fun navigate(@Media.Kind kindOfMedia: Int, mediaId: Long): Completable = when(kindOfMedia) {

        Media.ALBUM ->
            albumRepository.getItem(mediaId)
                .subscribeOn(schedulerProvider.worker())
                .observeOn(schedulerProvider.main())
                .firstOrError()
                .doOnSuccess { appRouter.openAlbum(it) }
                .ignoreElement()

        Media.ARTIST ->
            artistRepository.getItem(mediaId)
                .subscribeOn(schedulerProvider.worker())
                .observeOn(schedulerProvider.main())
                .firstOrError()
                .doOnSuccess { appRouter.openArtist(it) }
                .ignoreElement()

        Media.GENRE ->
            genreRepository.getItem(mediaId)
                .subscribeOn(schedulerProvider.worker())
                .observeOn(schedulerProvider.main())
                .firstOrError()
                .doOnSuccess { appRouter.openGenre(it) }
                .ignoreElement()

        Media.MY_FILE ->
            myFileRepository.getItem(mediaId)
                .subscribeOn(schedulerProvider.worker())
                .observeOn(schedulerProvider.main())
                .firstOrError()
                .doOnSuccess { appRouter.openMyFile(it) }
                .ignoreElement()

        Media.PLAYLIST ->
            playlistRepository.getItem(mediaId)
                .subscribeOn(schedulerProvider.worker())
                .observeOn(schedulerProvider.main())
                .firstOrError()
                .doOnSuccess { appRouter.openPlaylist(it) }
                .ignoreElement()

        Media.SONG ->
            songRepository.getItem(mediaId)
                .subscribeOn(schedulerProvider.worker())
                .observeOn(schedulerProvider.main())
                .firstOrError()
                .doOnSuccess { song ->
                    val queue = AudioSourceQueue(song)
                    player.prepareByTarget(queue, song.toAudioSource(), true)
                    appRouter.openPlayer()
                }
                .ignoreElement()

        else -> Completable.error(IllegalArgumentException("Unknown kind of media: $kindOfMedia"))
    }

}