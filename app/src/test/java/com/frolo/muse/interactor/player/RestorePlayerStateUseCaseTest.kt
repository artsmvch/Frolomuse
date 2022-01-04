package com.frolo.muse.interactor.player

import com.frolo.muse.TestSchedulerProvider
import com.frolo.muse.common.AudioSourceQueue
import com.frolo.player.Player
import com.frolo.player.AudioSourceQueue
import com.frolo.muse.common.toAudioSource
import com.frolo.muse.common.toAudioSources
import com.frolo.muse.mockSong
import com.frolo.muse.mockSongList
import com.frolo.muse.model.media.Album
import com.frolo.muse.model.media.Song
import com.frolo.muse.repository.*
import com.frolo.test.mockKT
import com.frolo.test.mockList
import com.frolo.test.randomLong
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
                preferences
        )
    }

    @Test
    fun test_restoreState_Success() {
        val id = 1L
        val album = Album(id, "album", "artist", 10)
        val songs = mockSongList(size = 10, allowIdCollisions = false)
        val targetSong = songs.first()
        val songQueue = AudioSourceQueue(songs, null)
        val playbackPosition = 1337

        whenever(preferences.lastMediaCollectionType)
                .thenReturn(-1)

        whenever(preferences.lastMediaCollectionId)
                .thenReturn(id)

        whenever(preferences.lastMediaCollectionItemIds)
                .thenReturn(Flowable.just(songs.map { song -> song.id }))

        whenever(albumRepository.getItem(eq(id)))
                .thenReturn(Flowable.just(album))

        whenever(albumRepository.collectSongs(eq(album)))
                .thenReturn(Single.just(songs))

        whenever(preferences.lastSongId)
                .thenReturn(targetSong.id)

        whenever(preferences.lastPlaybackPosition)
                .thenReturn(playbackPosition)

        whenever(songRepository.allItems)
                .thenReturn(Flowable.just(mockList(size = 100)))

        whenever(songRepository.getSongsOptionally(eq(songs.map { song -> song.id })))
                .thenReturn(Flowable.just(songs))

        whenever(songRepository.getItem(eq(targetSong.id)))
                .thenReturn(Flowable.just(targetSong))

        val observer = TestObserver.create<Unit>()

        restorePlayerStateUseCase.restorePlayerStateIfNeeded(player)
                .subscribe(observer)

        observer.assertComplete()

        verify(player, times(1)).prepareByTarget(
            queue = argThat { deepEquals(songQueue) },
            target = eq(targetSong.toAudioSource()),
            startPlaying = eq(false),
            playbackPosition = eq(playbackPosition)
        )
    }

    @Test
    fun test_restoreState_SuccessDefault() {
        val albumId = randomLong()
        val album = Album(albumId, "album", "artist", 0)
        val albumSongs = mockSongList(size = 0, allowIdCollisions = false)
        val lastPlayedSong = mockSong(albumId = albumId)
        val playbackPosition = 1337
        val allSongs = mockSongList(size = 100, allowIdCollisions = false)
        val expectedSong = allSongs.first()
        val expectedQueue = AudioSourceQueue.create(allSongs.toAudioSources())

        whenever(preferences.lastMediaCollectionType)
                .thenReturn(-1)

        whenever(preferences.lastMediaCollectionId)
                .thenReturn(albumId)

        whenever(preferences.lastMediaCollectionItemIds)
                .thenReturn(Flowable.just(albumSongs.map { song -> song.id }))

        whenever(albumRepository.getItem(eq(albumId)))
                .thenReturn(Flowable.just(album))

        whenever(albumRepository.collectSongs(eq(album)))
                .thenReturn(Single.just(albumSongs))

        whenever(preferences.lastSongId)
                .thenReturn(lastPlayedSong.id)

        whenever(preferences.lastPlaybackPosition)
                .thenReturn(playbackPosition)

        whenever(songRepository.allItems)
                .thenReturn(Flowable.just(allSongs))

        whenever(songRepository.getItem(eq(lastPlayedSong.id)))
                .thenReturn(Flowable.just(lastPlayedSong))

        val observer = TestObserver.create<Unit>()

        restorePlayerStateUseCase.restorePlayerStateIfNeeded(player)
                .subscribe(observer)

        observer.assertComplete()

        verify(player, times(1)).prepareByTarget(
            argThat { deepEquals(expectedQueue) }, eq(expectedSong.toAudioSource()),
            eq(false), eq(0))
    }

    @Test
    fun test_restoreState_Failure() {
        val id = 1L
        val album = Album(id, "album", "artist", 10)
        val songs = mockSongList(size = 0, allowIdCollisions = false)
        val targetSong = mockKT<Song>()
        val playbackPosition = 1337
        val allSongs = mockSongList(size = 0, allowIdCollisions = false)

        whenever(preferences.lastMediaCollectionType)
                .thenReturn(-1)

        whenever(preferences.lastMediaCollectionId)
                .thenReturn(id)

        whenever(preferences.lastMediaCollectionItemIds)
                .thenReturn(Flowable.just(songs.map { song -> song.id }))

        whenever(albumRepository.getItem(eq(id)))
                .thenReturn(Flowable.just(album))

        whenever(albumRepository.collectSongs(eq(album)))
                .thenReturn(Single.just(songs))

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
                .prepareByTarget(any(), any(), any(), any())
    }

}