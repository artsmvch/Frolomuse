package com.frolo.muse.interactor.media.get

import com.frolo.muse.*
import com.frolo.muse.model.Library
import com.frolo.muse.model.media.Album
import com.frolo.muse.model.media.Song
import com.frolo.muse.model.menu.SortOrderMenu
import com.frolo.muse.model.sort.SortOrder
import com.frolo.muse.repository.AlbumChunkRepository
import com.frolo.muse.repository.Preferences
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.subscribers.TestSubscriber
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.MockitoAnnotations


@RunWith(JUnit4::class)
class GetAlbumSongsUseCaseTest {

    private val schedulerProvider = TestSchedulerProvider.SHARED
    @Mock
    private lateinit var repository: AlbumChunkRepository
    @Mock
    private lateinit var preferences: Preferences

    private lateinit var getAlbumSongsUseCase: GetAlbumSongsUseCase

    // Test values
    private lateinit var album: Album

    private val sortOrder1 = TestSortOrder("Sort Order 1","sort_order_1")
    private val result1: List<Song> = List(size = 5) { index ->
        mockSong(
            duration = (5 + 10 * index) * 1_000
        )
    }

    private val sortOrder2 = TestSortOrder("Sort Order 2","sort_order_2")
    private val result2: List<Song> = List(size = 10) { index ->
        mockSong(
            duration = (5 + 10 * index) * 1_000
        )
    }

    private val sortOrders: MutableList<SortOrder> = arrayListOf(sortOrder1, sortOrder2)

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        album = mockKT()
        getAlbumSongsUseCase = GetAlbumSongsUseCase(
                schedulerProvider,
                repository,
                preferences,
                album)

        whenever(repository.getSongsFromAlbum(eq(album), eq(sortOrder1.key)))
                .thenReturn(Flowable.just(result1))

        whenever(repository.getSongsFromAlbum(eq(album), eq(sortOrder2.key)))
                .thenReturn(Flowable.just(result2))

        whenever(repository.sortOrders)
                .doReturn(Single.just(sortOrders))
    }

    @Test
    fun test_getMediaList_Success() {
        // Test with sort order 1, no audio duration limit
        run {
            val subscriber = TestSubscriber.create<List<Song>>()

            whenever(preferences.getSortOrderForSection(eq(Library.ALBUM)))
                    .doReturn(Flowable.just(sortOrder1.key))

            whenever(preferences.isSortOrderReversedForSection(eq(Library.ALBUM)))
                    .doReturn(Flowable.just(false))

            whenever(preferences.minAudioFileDuration)
                    .doReturn(Flowable.just(0))

            getAlbumSongsUseCase.getMediaList()
                    .subscribe(subscriber)

            val expectedOutput = result1.toList()
            subscriber.assertResult(expectedOutput)
        }

        // Test with sort order 1, limited audio duration
        run {
            val subscriber = TestSubscriber.create<List<Song>>()

            whenever(preferences.getSortOrderForSection(eq(Library.ALBUM)))
                    .doReturn(Flowable.just(sortOrder1.key))

            whenever(preferences.isSortOrderReversedForSection(eq(Library.ALBUM)))
                    .doReturn(Flowable.just(false))

            whenever(preferences.minAudioFileDuration)
                    .doReturn(Flowable.just(10))

            getAlbumSongsUseCase.getMediaList()
                    .subscribe(subscriber)

            val expectedOutput2 = result1.filter { it.duration >= 10_000 }
            subscriber.assertResult(expectedOutput2)
        }

        // Test with sort order 2, no audio duration limit
        run {
            val subscriber = TestSubscriber.create<List<Song>>()

            whenever(preferences.getSortOrderForSection(eq(Library.ALBUM)))
                    .doReturn(Flowable.just(sortOrder2.key))

            whenever(preferences.isSortOrderReversedForSection(eq(Library.ALBUM)))
                    .doReturn(Flowable.just(true))

            whenever(preferences.minAudioFileDuration)
                    .doReturn(Flowable.just(0))

            getAlbumSongsUseCase.getMediaList()
                    .subscribe(subscriber)

            val expectedOutput = result2.reversed()
            subscriber.assertResult(expectedOutput)
        }

        // Test with sort order 2, limited audio duration
        run {
            val subscriber = TestSubscriber.create<List<Song>>()

            whenever(preferences.getSortOrderForSection(eq(Library.ALBUM)))
                    .doReturn(Flowable.just(sortOrder2.key))

            whenever(preferences.isSortOrderReversedForSection(eq(Library.ALBUM)))
                    .doReturn(Flowable.just(true))

            whenever(preferences.minAudioFileDuration)
                    .doReturn(Flowable.just(20))

            getAlbumSongsUseCase.getMediaList()
                    .subscribe(subscriber)

            val expectedOutput = result2.reversed().filter { it.duration >= 20_000 }
            subscriber.assertResult(expectedOutput)
        }
    }

    @Test
    fun test_getMediaList_Failure() {
        val subscriber = TestSubscriber.create<List<Song>>()

        whenever(preferences.getSortOrderForSection(eq(Library.ALBUM)))
                .doReturn(Flowable.just(sortOrder1.key))

        whenever(preferences.isSortOrderReversedForSection(eq(Library.ALBUM)))
                .doReturn(Flowable.just(false))

        whenever(repository.getSongsFromAlbum(eq(album), eq(sortOrder1.key)))
                .doReturn(Flowable.error(UnsupportedOperationException()))

        whenever(preferences.minAudioFileDuration)
                .doReturn(Flowable.just(0))

        getAlbumSongsUseCase.getMediaList()
                .subscribe(subscriber)

        subscriber.assertError(UnsupportedOperationException::class.java)
    }

    @Test
    fun test_applySortOrder_Success() {
        val observer = TestObserver.create<List<Song>>()

        whenever(preferences.saveSortOrderForSection(eq(Library.ALBUM), eq(sortOrder1.key)))
                .doReturn(Completable.complete())

        getAlbumSongsUseCase.applySortOrder(sortOrder1)
                .subscribe(observer)

        observer.assertComplete()
    }

    @Test
    fun test_applySortOrder_Failure() {
        val observer = TestObserver.create<List<Song>>()

        whenever(preferences.saveSortOrderForSection(eq(Library.ALBUM), eq(sortOrder1.key)))
                .doReturn(Completable.error(UnsupportedOperationException()))

        getAlbumSongsUseCase.applySortOrder(sortOrder1)
                .subscribe(observer)

        observer.assertError(UnsupportedOperationException::class.java)
    }

    @Test
    fun test_getSortOrderMenu_Success() {
        run {
            val observer = TestObserver.create<SortOrderMenu>()

            whenever(preferences.getSortOrderForSection(eq(Library.ALBUM)))
                    .doReturn(Flowable.just(sortOrder1.key))

            whenever(preferences.isSortOrderReversedForSection(eq(Library.ALBUM)))
                    .doReturn(Flowable.just(true))

            getAlbumSongsUseCase.getSortOrderMenu()
                    .subscribe(observer)

            observer.assertResult(
                    SortOrderMenu(
                            sortOrders,
                            sortOrder1,
                            true
                    )
            )
        }
    }

    @Test
    fun test_getSortOrderMenu_Failure() {
        run {
            val observer = TestObserver.create<SortOrderMenu>()

            whenever(repository.sortOrders)
                    .doReturn(Single.error(UnsupportedOperationException()))

            whenever(preferences.getSortOrderForSection(eq(Library.ALBUM)))
                    .doReturn(Flowable.just(sortOrder1.key))

            whenever(preferences.isSortOrderReversedForSection(eq(Library.ALBUM)))
                    .doReturn(Flowable.just(true))

            getAlbumSongsUseCase.getSortOrderMenu()
                    .subscribe(observer)

            observer.assertFailure(UnsupportedOperationException::class.java)
        }
    }

}