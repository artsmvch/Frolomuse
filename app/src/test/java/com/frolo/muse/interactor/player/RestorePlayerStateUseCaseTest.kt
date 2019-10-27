package com.frolo.muse.interactor.player

import com.frolo.muse.TestSchedulerProvider
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.SongQueue
import com.frolo.muse.engine.SongQueueFactory
import com.frolo.muse.mockKT
import com.frolo.muse.mockList
import com.frolo.muse.mockSongList
import com.frolo.muse.model.media.Album
import com.frolo.muse.model.media.Song
import com.frolo.muse.repository.*
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.MockitoAnnotations


@RunWith(JUnit4::class)
class RestorePlayerStateUseCaseTest {

    private val schedulerProvider = TestSchedulerProvider.SHARED

    @Mock
    private lateinit var songRepository: SongRepository
    @Mock
    private lateinit var albumRepository: AlbumRepository
    @Mock
    private lateinit var artistRepository: ArtistRepository
    @Mock
    private lateinit var genreRepository: GenreRepository
    @Mock
    private lateinit var playlistRepository: PlaylistRepository
    @Mock
    private lateinit var preferences: Preferences
    @Mock
    private lateinit var songQueueFactory: SongQueueFactory

    private lateinit var restorePlayerStateUseCase: RestorePlayerStateUseCase

    @Mock
    private lateinit var player: Player

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        restorePlayerStateUseCase = RestorePlayerStateUseCase(
                schedulerProvider,
                songRepository,
                albumRepository,
                artistRepository,
                genreRepository,
                playlistRepository,
                preferences,
                songQueueFactory
        )
    }

    @Test
    fun test_restoreState_Success() {
        val type = SongQueue.ALBUM
        val id = 1L
        val album = Album(id, "album", "artist", 10)
        val songs = mockSongList(size = 10, allowIdCollisions = false)
        val targetSong = songs.first()
        val songQueue = SongQueue.create(type, album.id, album.name, songs)
        val playbackPosition = 1337

        whenever(preferences.lastMediaCollectionType)
                .thenReturn(type)

        whenever(preferences.lastMediaCollectionId)
                .thenReturn(id)

        whenever(albumRepository.getItem(eq(id)))
                .thenReturn(Flowable.just(album))

        whenever(albumRepository.collectSongs(eq(album)))
                .thenReturn(Single.just(songs))

        whenever(songQueueFactory.create(
                eq(type),
                eq(id),
                eq(album.name),
                eq(songs)
        )).thenReturn(songQueue)

        whenever(preferences.lastSongId)
                .thenReturn(targetSong.id)

        whenever(preferences.lastPlaybackPosition)
                .thenReturn(playbackPosition)

        whenever(songRepository.allItems)
                .thenReturn(Flowable.just(mockList(size = 100)))

        whenever(songRepository.getItem(eq(targetSong.id)))
                .thenReturn(Flowable.just(targetSong))

        val observer = TestObserver.create<Unit>()

        restorePlayerStateUseCase.restorePlayerStateIfNeeded(player)
                .subscribe(observer)

        observer.assertComplete()

        verify(player, times(1))
                .prepare(songQueue, targetSong, playbackPosition, false)
    }

    @Test
    fun test_restoreState_SuccessDefault() {
        val type = SongQueue.ALBUM
        val id = 1L
        val album = Album(id, "album", "artist", 10)
        val songs = mockSongList(size = 0, allowIdCollisions = false)
        val targetSong = mockKT<Song>()
        val playbackPosition = 1337
        val allSongs = mockSongList(size = 100, allowIdCollisions = false)
        val defaultTargetSong = allSongs.first()
        val songQueue = SongQueue.create(
                type, album.id, album.name, songs)
        val defaultSongQueue = SongQueue.create(
                SongQueue.CHUNK, SongQueue.NO_ID, "", allSongs)

        whenever(preferences.lastMediaCollectionType)
                .thenReturn(type)

        whenever(preferences.lastMediaCollectionId)
                .thenReturn(id)

        whenever(albumRepository.getItem(eq(id)))
                .thenReturn(Flowable.just(album))

        whenever(albumRepository.collectSongs(eq(album)))
                .thenReturn(Single.just(songs))

        whenever(songQueueFactory.create(
                eq(type),
                eq(id),
                eq(album.name),
                eq(songs)
        )).thenReturn(songQueue)

        whenever(songQueueFactory.create(
                eq(SongQueue.CHUNK),
                eq(SongQueue.NO_ID),
                eq(""),
                eq(allSongs)
        )).thenReturn(defaultSongQueue)

        whenever(preferences.lastSongId)
                .thenReturn(targetSong.id)

        whenever(preferences.lastPlaybackPosition)
                .thenReturn(playbackPosition)

        whenever(songRepository.allItems)
                .thenReturn(Flowable.just(allSongs))

        whenever(songRepository.getItem(eq(targetSong.id)))
                .thenReturn(Flowable.just(targetSong))

        val observer = TestObserver.create<Unit>()

        restorePlayerStateUseCase.restorePlayerStateIfNeeded(player)
                .subscribe(observer)

        observer.assertComplete()

        verify(player, times(1))
                .prepare(defaultSongQueue, defaultTargetSong, 0, false)
    }

    @Test
    fun test_restoreState_Failure() {
        val type = SongQueue.ALBUM
        val id = 1L
        val album = Album(id, "album", "artist", 10)
        val songs = mockSongList(size = 0, allowIdCollisions = false)
        val targetSong = mockKT<Song>()
        val playbackPosition = 1337
        val allSongs = mockSongList(size = 0, allowIdCollisions = false)
        val songQueue = SongQueue.create(type, album.id, album.name, allSongs)

        whenever(preferences.lastMediaCollectionType)
                .thenReturn(type)

        whenever(preferences.lastMediaCollectionId)
                .thenReturn(id)

        whenever(albumRepository.getItem(eq(id)))
                .thenReturn(Flowable.just(album))

        whenever(albumRepository.collectSongs(eq(album)))
                .thenReturn(Single.just(songs))

        whenever(songQueueFactory.create(
                eq(type),
                eq(id),
                eq(album.name),
                eq(allSongs)
        )).thenReturn(songQueue)

        whenever(songQueueFactory.create(
                eq(SongQueue.CHUNK),
                eq(SongQueue.NO_ID),
                eq(""),
                eq(songs)
        )).thenReturn(songQueue)

        whenever(preferences.lastSongId)
                .thenReturn(targetSong.id)

        whenever(preferences.lastPlaybackPosition)
                .thenReturn(playbackPosition)

        whenever(songRepository.allItems)
                .thenReturn(Flowable.just(allSongs))

        whenever(songRepository.getItem(eq(targetSong.id)))
                .thenReturn(Flowable.just(targetSong))

        val observer = TestObserver.create<Unit>()

        restorePlayerStateUseCase.restorePlayerStateIfNeeded(player)
                .subscribe(observer)

        observer.assertError(IndexOutOfBoundsException::class.java)

        verify(player, never())
                .prepare(any(), any(), any(), any())
    }

}