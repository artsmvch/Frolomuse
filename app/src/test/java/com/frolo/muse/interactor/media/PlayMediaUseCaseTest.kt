package com.frolo.muse.interactor.media

import com.frolo.muse.*
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.SongQueue
import com.frolo.muse.engine.SongQueueFactory
import com.frolo.muse.model.media.Media
import com.frolo.muse.model.media.Song
import com.frolo.muse.repository.MediaRepository
import com.frolo.muse.repository.Preferences
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.MockitoAnnotations


@RunWith(JUnit4::class)
class PlayMediaUseCaseTest {

    private val schedulerProvider = TestSchedulerProvider.SHARED

    @Mock
    private lateinit var repository: MediaRepository<Media>
    @Mock
    private lateinit var preferences: Preferences
    @Mock
    private lateinit var player: Player
    @Mock
    private lateinit var songQueueFactory: SongQueueFactory

    private lateinit var playMediaUseCase: PlayMediaUseCase<Media>

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        playMediaUseCase = PlayMediaUseCase(
                schedulerProvider,
                repository,
                preferences,
                player,
                songQueueFactory
        )
    }

    @Test
    fun test_PlayMultiple() {
        val items = mockMediaList(size = 10)

        val songs = mockList<Song>(size = 100)

        val targetSong = songs.first()

        val songQueue = SongQueue.create(
                SongQueue.CHUNK,
                SongQueue.NO_ID,
                "SongQueue",
                songs)

        whenever(repository.collectSongs(eq(items)))
                .thenReturn(Single.just(songs))

        whenever(preferences.saveLastMediaCollectionType(any()))
                .thenDoNothing()

        whenever(preferences.saveLastMediaCollectionId(any()))
                .thenDoNothing()

        whenever(preferences.saveLastSongId(any()))
                .thenDoNothing()

        whenever(songQueueFactory.create(eq(items), eq(songs)))
                .thenReturn(songQueue)

        whenever(player.prepare(any(), any(), any(), any()))
                .thenDoNothing()

        playMediaUseCase.play(items)
                .subscribe()

        verify(player, times(1))
                .prepare(songQueue, targetSong, true)
    }

    @Test
    fun test_PlaySingle() {
        val item = mockMedia()

        val songs = mockList<Song>(size = 100)

        val targetSong = songs.first()

        val songQueue = SongQueue.create(
                SongQueue.CHUNK,
                SongQueue.NO_ID,
                "SongQueue",
                songs)

        whenever(repository.collectSongs(eq(item)))
                .thenReturn(Single.just(songs))

        whenever(repository.collectSongs(eq(listOf(item))))
                .thenReturn(Single.just(songs))

        whenever(preferences.saveLastMediaCollectionType(any()))
                .thenDoNothing()

        whenever(preferences.saveLastMediaCollectionId(any()))
                .thenDoNothing()

        whenever(preferences.saveLastSongId(any()))
                .thenDoNothing()

        whenever(songQueueFactory.create(eq(listOf(item)), eq(songs)))
                .thenReturn(songQueue)

        whenever(player.prepare(any(), any(), any(), any()))
                .thenDoNothing()

        playMediaUseCase.play(item)
                .subscribe()

        verify(player, times(1))
                .prepare(songQueue, targetSong, true)
    }

    @Test
    fun test_PlayNextMultiple() {
        val items = mockMediaList(size = 10)

        val songs = mockList<Song>(size = 100)

        whenever(repository.collectSongs(eq(items)))
                .thenReturn(Single.just(songs))

        whenever(player.addAllNext(any()))
                .thenDoNothing()

        playMediaUseCase.playNext(items)
                .subscribe()

        verify(player, times(1))
                .addAllNext(songs)
    }

    @Test
    fun test_PlayNextSingle() {
        val item = mockMedia()

        val songs = mockList<Song>(size = 100)

        whenever(repository.collectSongs(eq(listOf(item))))
                .thenReturn(Single.just(songs))

        whenever(player.addAllNext(any()))
                .thenDoNothing()

        playMediaUseCase.playNext(item)
                .subscribe()

        verify(player, times(1))
                .addAllNext(songs)
    }

    @Test
    fun test_AddToQueueMultiple() {
        val items = mockMediaList(size = 10)

        val songs = mockList<Song>(size = 100)

        whenever(repository.collectSongs(eq(items)))
                .thenReturn(Single.just(songs))

        whenever(player.addAll(any()))
                .thenDoNothing()

        playMediaUseCase.addToQueue(items)
                .subscribe()

        verify(player, times(1))
                .addAll(songs)
    }

    @Test
    fun test_AddToQueueSingle() {
        val item = mockMedia()

        val songs = mockList<Song>(size = 100)

        whenever(repository.collectSongs(eq(listOf(item))))
                .thenReturn(Single.just(songs))

        whenever(player.addAll(any()))
                .thenDoNothing()

        playMediaUseCase.addToQueue(item)
                .subscribe()

        verify(player, times(1))
                .addAll(songs)
    }
}