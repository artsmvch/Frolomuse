package com.frolo.muse.interactor.media.get

import com.frolo.muse.TestSchedulerProvider
import com.frolo.muse.mockKT
import com.frolo.muse.mockList
import com.frolo.muse.model.media.Album
import com.frolo.muse.model.media.Artist
import com.frolo.muse.model.media.Media
import com.frolo.muse.model.menu.SortOrderMenu
import com.frolo.muse.repository.AlbumRepository
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.observers.TestObserver
import io.reactivex.subscribers.TestSubscriber
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.MockitoAnnotations


@RunWith(JUnit4::class)
class GetAlbumsOfArtistUseCaseTest {

    @Mock
    private lateinit var repository: AlbumRepository

    private val schedulerProvider = TestSchedulerProvider.SHARED

    private lateinit var artist: Artist

    private lateinit var getAlbumsOfArtistUseCase: GetAlbumsOfArtistUseCase

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        artist = mockKT()
        getAlbumsOfArtistUseCase = GetAlbumsOfArtistUseCase(
                repository,
                schedulerProvider,
                artist)
    }

    @Test
    fun test_getSortOrderMenu() {
        val observer = TestObserver.create<SortOrderMenu>()

        getAlbumsOfArtistUseCase.getSortOrderMenu()
                .subscribe(observer)

        observer.assertError(UnsupportedOperationException::class.java)
    }

    @Test
    fun test_applySortOrder() {
        val subscriber = TestSubscriber.create<List<Media>>()

        getAlbumsOfArtistUseCase.applySortOrder("test")
                .subscribe(subscriber)

        subscriber.assertError(UnsupportedOperationException::class.java)
    }

    @Test
    fun test_applySortOrderReversed() {
        run {
            val subscriber = TestSubscriber.create<List<Media>>()

            getAlbumsOfArtistUseCase.applySortOrderReversed(true)
                    .subscribe(subscriber)

            subscriber.assertError(UnsupportedOperationException::class.java)
        }

        run {
            val subscriber = TestSubscriber.create<List<Media>>()

            getAlbumsOfArtistUseCase.applySortOrderReversed(false)
                    .subscribe(subscriber)

            subscriber.assertError(UnsupportedOperationException::class.java)
        }
    }

    @Test
    fun test_getMediaList_Success() {
        val subscriber = TestSubscriber.create<List<Media>>()

        val testList: List<Album> = mockList(size = 10)

        whenever(repository.getAlbumsOfArtist(any()))
                .thenReturn(Flowable.just(testList))

        getAlbumsOfArtistUseCase.getMediaList()
                .subscribe(subscriber)

        subscriber.assertResult(testList)
    }

    @Test
    fun test_getMediaList_Failure() {
        val subscriber = TestSubscriber.create<List<Media>>()

        whenever(repository.getAlbumsOfArtist(any()))
                .thenReturn(Flowable.error(RuntimeException()))

        getAlbumsOfArtistUseCase.getMediaList()
                .subscribe(subscriber)

        subscriber.assertError(RuntimeException::class.java)
    }

}