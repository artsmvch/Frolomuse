package com.frolo.muse.interactor.media

import com.frolo.muse.*
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.AudioSourceQueue
import com.frolo.muse.common.AudioSourceQueueFactory
import com.frolo.muse.common.toAudioSource
import com.frolo.muse.common.toAudioSources
import com.frolo.muse.model.media.*
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.repository.GenericMediaRepository
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.MockitoAnnotations


@RunWith(JUnit4::class)
class ClickMediaUseCaseTest {

    private lateinit var clickMediaUseCase: ClickMediaUseCase<Media>

    @Mock
    private lateinit var player: Player
    @Mock
    private lateinit var repository: GenericMediaRepository
    @Mock
    private lateinit var audioSourceQueueFactory: AudioSourceQueueFactory
    @Mock
    private lateinit var navigator: Navigator

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        clickMediaUseCase = ClickMediaUseCase(
                TestSchedulerProvider.SHARED,
                player,
                repository,
                navigator,
                audioSourceQueueFactory
        )
    }

    @Test
    fun test_clickOnSong_Play() {
        val songs = mockList<Song>(size = 10)
        val song = mockKT<Song>()

        val songQueue = AudioSourceQueue.create(AudioSourceQueue.CHUNK, AudioSourceQueue.NO_ID, "test", songs.toAudioSources())

        val testObserver = TestObserver.create<Unit>()

        whenever(player.getCurrent())
                .thenReturn(null)

        whenever(audioSourceQueueFactory.create(eq(listOf(song)), eq(songs)))
                .thenReturn(songQueue)

        whenever(player.prepare(eq(songQueue), eq(song.toAudioSource()), eq(true)))
                .thenDoNothing()

        clickMediaUseCase.click(song, songs)
                .subscribe(testObserver)

        testObserver.assertComplete()

        verify(player, times(1))
                .prepare(songQueue, song.toAudioSource(), true)
    }

    @Test
    fun test_clickOnSong_Toggle() {
        val collection = mockList<Song>(size = 10)
        val song = mockKT<Song>()

        val testObserver = TestObserver.create<Unit>()

        whenever(player.getCurrent())
                .thenReturn(song.toAudioSource())

        whenever(player.toggle())
                .thenDoNothing()

        clickMediaUseCase.click(song, collection)
                .subscribe(testObserver)

        testObserver.assertComplete()

        verify(player, times(1))
                .toggle()
    }

    @Test
    fun test_clickOnAlbum_OpenAlbum() {
        val collection = mockList<Album>(size = 10)
        val album = mockKT<Album>()

        val testObserver = TestObserver.create<Unit>()

        whenever(navigator.openAlbum(eq(album)))
                .thenDoNothing()

        clickMediaUseCase.click(album, collection)
                .subscribe(testObserver)

        testObserver.assertComplete()

        verify(navigator, times(1))
                .openAlbum(album)
    }

    @Test
    fun test_clickOnArtist_OpenArtist() {
        val collection = mockList<Artist>(size = 10)
        val artist = mockKT<Artist>()

        val testObserver = TestObserver.create<Unit>()

        whenever(navigator.openArtist(eq(artist)))
                .thenDoNothing()

        clickMediaUseCase.click(artist, collection)
                .subscribe(testObserver)

        testObserver.assertComplete()

        verify(navigator, times(1))
                .openArtist(artist)
    }

    @Test
    fun test_clickOnGenre_OpenGenre() {
        val collection = mockList<Genre>(size = 10)
        val genre = mockKT<Genre>()

        val testObserver = TestObserver.create<Unit>()

        whenever(navigator.openGenre(eq(genre)))
                .thenDoNothing()

        clickMediaUseCase.click(genre, collection)
                .subscribe(testObserver)

        testObserver.assertComplete()

        verify(navigator, times(1))
                .openGenre(genre)
    }

    @Test
    fun test_clickOnPlaylist_OpenPlaylist() {
        val collection = mockList<Playlist>(size = 10)
        val playlist = mockKT<Playlist>()

        val testObserver = TestObserver.create<Unit>()

        whenever(navigator.openPlaylist(eq(playlist)))
                .thenDoNothing()

        clickMediaUseCase.click(playlist, collection)
                .subscribe(testObserver)

        testObserver.assertComplete()

        verify(navigator, times(1))
                .openPlaylist(playlist)
    }

    @Test
    fun test_clickOnMyFile_OpenFolder() {
        val collection = mock<List<MyFile>>()
        val myFile = mock<MyFile>()

        whenever(myFile.kind)
                .thenReturn(Media.MY_FILE)

        whenever(myFile.isSongFile)
                .thenReturn(false)

        whenever(myFile.isDirectory)
                .thenReturn(true)

        val testObserver = TestObserver.create<Unit>()

        whenever(navigator.openMyFile(myFile))
                .thenDoNothing()

        clickMediaUseCase.click(myFile, collection)
                .subscribe(testObserver)

        testObserver.assertComplete()

        verify(navigator, times(1))
                .openMyFile(myFile)
    }

    @Test
    fun test_clickOnMyFile_PlaySongFiles() {
        val collection = ArrayList<MyFile>().also { list ->
            // Directory
            mock<MyFile>().also { myFile ->
                whenever(myFile.kind).thenReturn(Media.MY_FILE)
                whenever(myFile.isDirectory).thenReturn(true)
                whenever(myFile.isSongFile).thenReturn(false)
                list.add(myFile)
            }
            // Song file
            mock<MyFile>().also { myFile ->
                whenever(myFile.kind).thenReturn(Media.MY_FILE)
                whenever(myFile.isDirectory).thenReturn(false)
                whenever(myFile.isSongFile).thenReturn(true)
                list.add(myFile)
            }
            // Song file
            mock<MyFile>().also { myFile ->
                whenever(myFile.kind).thenReturn(Media.MY_FILE)
                whenever(myFile.isDirectory).thenReturn(false)
                whenever(myFile.isSongFile).thenReturn(true)
                list.add(myFile)
            }
            // Just a file
            mock<MyFile>().also { myFile ->
                whenever(myFile.kind).thenReturn(Media.MY_FILE)
                whenever(myFile.isDirectory).thenReturn(false)
                whenever(myFile.isSongFile).thenReturn(false)
                list.add(myFile)
            }
        }
        val myFile = mock<MyFile>()

        val testObserver = TestObserver.create<Unit>()

        val song = mockKT<Song>()
        val songsFromMyFile = listOf(song)
        val allSongs = songsFromMyFile + songsFromMyFile
        val songQueue = AudioSourceQueue.create(AudioSourceQueue.FOLDER, AudioSourceQueue.NO_ID, "test", allSongs.toAudioSources())

        whenever(myFile.kind).thenReturn(Media.MY_FILE)

        whenever(myFile.isSongFile).thenReturn(true)

        whenever(myFile.isDirectory).thenReturn(false)

        whenever(navigator.openMyFile(myFile)).thenDoNothing()

        whenever(repository.collectSongs(any<MyFile>())).thenReturn(Single.just(songsFromMyFile))

        whenever(player.getCurrent()).thenReturn(null)

        whenever(audioSourceQueueFactory.create(eq(listOf(song)), eq(allSongs))).thenReturn(songQueue)

        whenever(player.prepare(eq(songQueue), eq(song.toAudioSource()), eq(true))).thenDoNothing()

        clickMediaUseCase.click(myFile, collection)
                .subscribe(testObserver)

        testObserver.assertComplete()

        verify(player, times(1))
                .prepare(songQueue, song.toAudioSource(), true)
    }

    @Test
    fun test_clickOnMyFile_NoAction() {
        val collection = mock<List<MyFile>>()
        val myFile = mock<MyFile>()

        val testObserver = TestObserver.create<Unit>()

        whenever(myFile.kind).thenReturn(Media.MY_FILE)

        whenever(myFile.isSongFile).thenReturn(false)

        whenever(myFile.isDirectory).thenReturn(false)

        clickMediaUseCase.click(myFile, collection)
                .subscribe(testObserver)

        testObserver.assertComplete()
    }

    @Test
    fun test_clickOnUnknownMedia_Failure() {
        val collection = mock<List<Media>>()
        val media = mock<Media>()

        whenever(media.kind)
                .thenReturn(Media.NONE)

        val testObserver = TestObserver.create<Unit>()

        clickMediaUseCase.click(media, collection)
                .subscribe(testObserver)

        testObserver.assertError(UnknownMediaException::class.java)
    }

}