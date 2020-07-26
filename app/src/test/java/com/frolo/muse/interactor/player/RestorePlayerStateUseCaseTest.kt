package com.frolo.muse.interactor.player

import com.frolo.muse.TestSchedulerProvider
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.AudioSourceQueue
import com.frolo.muse.common.AudioSourceQueueFactory
import com.frolo.muse.common.toAudioSource
import com.frolo.muse.common.toAudioSources
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
    private lateinit var audioSourceQueueFactory: AudioSourceQueueFactory

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
                audioSourceQueueFactory
        )
    }

    @Test
    fun test_restoreState_Success() {
        val type = AudioSourceQueue.ALBUM
        val id = 1L
        val album = Album(id, "album", "artist", 10)
        val songs = mockSongList(size = 10, allowIdCollisions = false)
        val targetSong = songs.first()
        val songQueue = AudioSourceQueue.create(type, album.id, album.name, songs.toAudioSources())
        val playbackPosition = 1337

        whenever(preferences.lastMediaCollectionType)
                .thenReturn(type)

        whenever(preferences.lastMediaCollectionId)
                .thenReturn(id)

        whenever(albumRepository.getItem(eq(id)))
                .thenReturn(Flowable.just(album))

        whenever(albumRepository.collectSongs(eq(album)))
                .thenReturn(Single.just(songs))

        whenever(audioSourceQueueFactory.create(
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
                .prepare(songQueue, targetSong.toAudioSource(), playbackPosition, false)
    }

    @Test
    fun test_restoreState_SuccessDefault() {
        val type = AudioSourceQueue.ALBUM
        val id = 1L
        val album = Album(id, "album", "artist", 10)
        val songs = mockSongList(size = 0, allowIdCollisions = false)
        val targetSong = mockKT<Song>()
        val playbackPosition = 1337
        val allSongs = mockSongList(size = 100, allowIdCollisions = false)
        val defaultTargetSong = allSongs.first()
        val songQueue = AudioSourceQueue.create(
                type, album.id, album.name, songs.toAudioSources())
        val defaultSongQueue = AudioSourceQueue.create(
                AudioSourceQueue.CHUNK, AudioSourceQueue.NO_ID, "", allSongs.toAudioSources())

        whenever(preferences.lastMediaCollectionType)
                .thenReturn(type)

        whenever(preferences.lastMediaCollectionId)
                .thenReturn(id)

        whenever(albumRepository.getItem(eq(id)))
                .thenReturn(Flowable.just(album))

        whenever(albumRepository.collectSongs(eq(album)))
                .thenReturn(Single.just(songs))

        whenever(audioSourceQueueFactory.create(
                eq(type),
                eq(id),
                eq(album.name),
                eq(songs)
        )).thenReturn(songQueue)

        whenever(audioSourceQueueFactory.create(
                eq(AudioSourceQueue.CHUNK),
                eq(AudioSourceQueue.NO_ID),
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
                .prepare(defaultSongQueue, defaultTargetSong.toAudioSource(), 0, false)
    }

    @Test
    fun test_restoreState_Failure() {
        val type = AudioSourceQueue.ALBUM
        val id = 1L
        val album = Album(id, "album", "artist", 10)
        val songs = mockSongList(size = 0, allowIdCollisions = false)
        val targetSong = mockKT<Song>()
        val playbackPosition = 1337
        val allSongs = mockSongList(size = 0, allowIdCollisions = false)
        val songQueue = AudioSourceQueue.create(type, album.id, album.name, allSongs.toAudioSources())

        whenever(preferences.lastMediaCollectionType)
                .thenReturn(type)

        whenever(preferences.lastMediaCollectionId)
                .thenReturn(id)

        whenever(albumRepository.getItem(eq(id)))
                .thenReturn(Flowable.just(album))

        whenever(albumRepository.collectSongs(eq(album)))
                .thenReturn(Single.just(songs))

        whenever(audioSourceQueueFactory.create(
                eq(type),
                eq(id),
                eq(album.name),
                eq(allSongs)
        )).thenReturn(songQueue)

        whenever(audioSourceQueueFactory.create(
                eq(AudioSourceQueue.CHUNK),
                eq(AudioSourceQueue.NO_ID),
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