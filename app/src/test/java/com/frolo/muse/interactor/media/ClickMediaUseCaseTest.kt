package com.frolo.muse.interactor.media

import com.frolo.muse.*
import com.frolo.player.Player
import com.frolo.player.AudioSourceQueue
import com.frolo.muse.common.toAudioSource
import com.frolo.muse.common.toAudioSources
import com.frolo.muse.router.AppRouter
import com.frolo.muse.repository.GenericMediaRepository
import com.frolo.music.model.*
import com.frolo.test.mockKT
import com.frolo.test.mockList
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
    private lateinit var appRouter: AppRouter

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        clickMediaUseCase = ClickMediaUseCase(
                TestSchedulerProvider.SHARED,
                player,
                repository,
                appRouter
        )
    }

    @Test
    fun test_clickOnSong_Play() {
        val songs = mockSongList(size = 10)
        val song = mockSong()

        val songQueue = AudioSourceQueue.create(songs.toAudioSources())

        val testObserver = TestObserver.create<Unit>()

        whenever(player.getCurrent())
                .thenReturn(null)

        whenever(player.prepareByTarget(eq(songQueue), eq(song.toAudioSource()), eq(true), eq(0)))
                .thenDoNothing()

        clickMediaUseCase.click(song, songs)
                .subscribe(testObserver)

        testObserver.assertComplete()

        verify(player, times(1))
                .prepareByTarget(argThat { deepEquals(songQueue) }, eq(song.toAudioSource()), eq(true), eq(0))
    }

    @Test
    fun test_clickOnSong_Toggle() {
        val collection = mockSongList(size = 10)
        val song = mockSong()

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

        whenever(appRouter.openAlbum(eq(album)))
                .thenDoNothing()

        clickMediaUseCase.click(album, collection)
                .subscribe(testObserver)

        testObserver.assertComplete()

        verify(appRouter, times(1))
                .openAlbum(album)
    }

    @Test
    fun test_clickOnArtist_OpenArtist() {
        val collection = mockList<Artist>(size = 10)
        val artist = mockKT<Artist>()

        val testObserver = TestObserver.create<Unit>()

        whenever(appRouter.openArtist(eq(artist)))
                .thenDoNothing()

        clickMediaUseCase.click(artist, collection)
                .subscribe(testObserver)

        testObserver.assertComplete()

        verify(appRouter, times(1))
                .openArtist(artist)
    }

    @Test
    fun test_clickOnGenre_OpenGenre() {
        val collection = mockList<Genre>(size = 10)
        val genre = mockKT<Genre>()

        val testObserver = TestObserver.create<Unit>()

        whenever(appRouter.openGenre(eq(genre)))
                .thenDoNothing()

        clickMediaUseCase.click(genre, collection)
                .subscribe(testObserver)

        testObserver.assertComplete()

        verify(appRouter, times(1))
                .openGenre(genre)
    }

    @Test
    fun test_clickOnPlaylist_OpenPlaylist() {
        val collection = mockList<Playlist>(size = 10)
        val playlist = mockKT<Playlist>()

        val testObserver = TestObserver.create<Unit>()

        whenever(appRouter.openPlaylist(eq(playlist)))
                .thenDoNothing()

        clickMediaUseCase.click(playlist, collection)
                .subscribe(testObserver)

        testObserver.assertComplete()

        verify(appRouter, times(1))
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

        whenever(appRouter.openMyFile(myFile))
                .thenDoNothing()

        clickMediaUseCase.click(myFile, collection)
                .subscribe(testObserver)

        testObserver.assertComplete()

        verify(appRouter, times(1))
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
        val songQueue = AudioSourceQueue.create(allSongs.toAudioSources())

        whenever(myFile.kind).thenReturn(Media.MY_FILE)

        whenever(myFile.isSongFile).thenReturn(true)

        whenever(myFile.isDirectory).thenReturn(false)

        whenever(appRouter.openMyFile(myFile)).thenDoNothing()

        whenever(repository.collectSongs(any<MyFile>())).thenReturn(Single.just(songsFromMyFile))

        whenever(player.getCurrent()).thenReturn(null)

        whenever(player.prepareByTarget(eq(songQueue), eq(song.toAudioSource()), eq(true), eq(0))).thenDoNothing()

        clickMediaUseCase.click(myFile, collection)
                .subscribe(testObserver)

        testObserver.assertComplete()

        verify(player, times(1))
                .prepareByTarget(argThat { deepEquals(songQueue) }, eq(song.toAudioSource()), eq(true), eq(0))
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