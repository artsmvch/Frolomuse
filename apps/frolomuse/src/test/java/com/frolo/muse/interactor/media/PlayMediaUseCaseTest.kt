package com.frolo.muse.interactor.media

import com.frolo.muse.*
import com.frolo.player.Player
import com.frolo.player.AudioSourceQueue
import com.frolo.muse.common.toAudioSource
import com.frolo.muse.common.toAudioSources
import com.frolo.music.model.Media
import com.frolo.music.model.Song
import com.frolo.music.repository.MediaRepository
import com.frolo.muse.repository.Preferences
import com.frolo.music.model.test.mockMedia
import com.frolo.music.model.test.mockMediaList
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import com.frolo.test.mockList


@RunWith(JUnit4::class)
class PlayMediaUseCaseTest {

    private val schedulerProvider = TestSchedulerProvider.SHARED

    @Mock
    private lateinit var repository: MediaRepository<Media>
    @Mock
    private lateinit var preferences: Preferences
    @Mock
    private lateinit var player: Player

    private lateinit var playMediaUseCase: PlayMediaUseCase<Media>

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        playMediaUseCase = PlayMediaUseCase(
                schedulerProvider,
                repository,
                player
        )
    }

    @Test
    fun test_PlayMultiple() {
        val items = mockMediaList(size = 10)

        val songs = mockList<Song>(size = 100)

        val targetSong = songs.first()

        val songQueue = AudioSourceQueue.create(songs.toAudioSources())

        whenever(repository.collectSongs(eq(items)))
                .thenReturn(Single.just(songs))

        whenever(preferences.saveLastMediaCollectionType(any()))
                .thenDoNothing()

        whenever(preferences.saveLastMediaCollectionId(any()))
                .thenDoNothing()

        whenever(preferences.saveLastSongId(any()))
                .thenDoNothing()

        whenever(player.prepareByTarget(any(), any(), any(), any()))
                .thenDoNothing()

        playMediaUseCase.play(items)
                .subscribe()

        verify(player, times(1))
                .prepareByTarget(argThat { deepEquals(songQueue) }, eq(targetSong.toAudioSource()), eq(true), eq(0))
    }

    @Test
    fun test_PlaySingle() {
        val item = mockMedia()

        val songs = mockList<Song>(size = 100)

        val targetSong = songs.first()

        val songQueue = AudioSourceQueue.create(songs.toAudioSources())

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

        whenever(player.prepareByTarget(any(), any(), any(), any()))
                .thenDoNothing()

        playMediaUseCase.play(item)
                .subscribe()

        verify(player, times(1))
                .prepareByTarget(argThat { deepEquals(songQueue) }, eq(targetSong.toAudioSource()), eq(true), eq(0))
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
                .addAllNext(songs.toAudioSources())
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
                .addAllNext(songs.toAudioSources())
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
                .addAll(songs.toAudioSources())
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
                .addAll(songs.toAudioSources())
    }
}