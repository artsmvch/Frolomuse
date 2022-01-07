package com.frolo.muse.interactor.media.get

import com.frolo.muse.TestSchedulerProvider
import com.frolo.muse.TestSortOrder
import com.frolo.muse.mockMediaList
import com.frolo.muse.model.Library
import com.frolo.music.model.Media
import com.frolo.muse.model.menu.SortOrderMenu
import com.frolo.muse.repository.MediaRepository
import com.frolo.muse.repository.Preferences
import com.frolo.muse.rx.SchedulerProvider
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.*
import io.reactivex.observers.TestObserver
import io.reactivex.subscribers.TestSubscriber
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.Mockito


@RunWith(JUnit4::class)
class GetSectionedMediaUseCaseTest {

    @Library.Section
    private val section: Int = Library.MIXED

    private val schedulerProvider: SchedulerProvider = TestSchedulerProvider.SHARED

    @Mock
    private lateinit var repository: MediaRepository<*>
    @Mock
    private lateinit var preferences: Preferences

    private lateinit var getSectionedMediaUseCase: GetSectionedMediaUseCase<*>

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        getSectionedMediaUseCase = Mockito.mock(
                GetSectionedMediaUseCase::class.java,
                Mockito.withSettings()
                        .useConstructor(section, schedulerProvider, repository, preferences)
                        .defaultAnswer(Mockito.CALLS_REAL_METHODS)
        )
    }

    @Test
    fun test_getMediaList_Success() {
        run {
            val subscriber = TestSubscriber.create<List<Media>>()

            /*mocking*/
            val sortOrder = "sort_order"
            val result = mockMediaList(size = 10)

            whenever(getSectionedMediaUseCase.getSortedCollection(eq(sortOrder)))
                    .thenReturn(Flowable.just(result))

            whenever(preferences.getSortOrderForSection(eq(section)))
                    .thenReturn(Flowable.just(sortOrder))

            whenever(preferences.isSortOrderReversedForSection(eq(section)))
                    .thenReturn(Flowable.just(true))

            /*calling method*/
            getSectionedMediaUseCase.getMediaList()
                    .subscribe(subscriber)

            /*testing*/
            subscriber.assertResult(result.reversed())
        }

        run {
            val subscriber = TestSubscriber.create<List<Media>>()

            /*mocking*/
            val sortOrder = "sort_order"
            val result = mockMediaList(size = 1)

            whenever(getSectionedMediaUseCase.getSortedCollection(eq(sortOrder)))
                    .thenReturn(Flowable.just(result))

            whenever(preferences.getSortOrderForSection(eq(section)))
                    .thenReturn(Flowable.just(sortOrder))

            whenever(preferences.isSortOrderReversedForSection(eq(section)))
                    .thenReturn(Flowable.just(false))

            /*calling method*/
            getSectionedMediaUseCase.getMediaList()
                    .subscribe(subscriber)

            /*testing*/
            subscriber.assertResult(result)
        }
    }

    @Test
    fun test_getMediaListDuringSortOrderAndReverseFlowChanges_Success() {
        // Testing flows of sort order change
        run {
            val subscriber = TestSubscriber.create<List<Media>>()

            /*mocking*/
            val sortOrder1 = "sort_order_1"
            val result1 = mockMediaList(size = 10)

            val sortOrder2 = "sort_order_2"
            val result2 = mockMediaList(size = 5)

            whenever(getSectionedMediaUseCase.getSortedCollection(eq(sortOrder1)))
                    .thenReturn(Flowable.just(result1))

            whenever(getSectionedMediaUseCase.getSortedCollection(eq(sortOrder2)))
                    .thenReturn(Flowable.just(result2))

            var sortOrderEmitter: Emitter<String>? = null

            whenever(preferences.getSortOrderForSection(eq(section)))
                    .thenReturn(
                            Flowable.create(
                                    { sortOrderEmitter = it },
                                    BackpressureStrategy.BUFFER
                            )
                    )

            var sortOrderReversedEmitter: Emitter<Boolean>? = null

            whenever(preferences.isSortOrderReversedForSection(eq(section)))
                    .thenReturn(
                            Flowable.create(
                                    { sortOrderReversedEmitter = it },
                                    BackpressureStrategy.BUFFER
                            )
                    )

            /*calling method*/
            getSectionedMediaUseCase.getMediaList()
                    .subscribe(subscriber)

            // emits sort order 1
            sortOrderEmitter!!.onNext(sortOrder1)

            subscriber.assertNoValues()

            // emits sort order reversed = false
            sortOrderReversedEmitter!!.onNext(false)

            subscriber.assertValueAt(0, result1)

            // emits sort order reversed = true
            sortOrderReversedEmitter!!.onNext(true)

            subscriber.assertValueAt(1, result1.asReversed())

            // emits sort order 2
            sortOrderEmitter!!.onNext(sortOrder2)

            subscriber.assertValueAt(2, result2.asReversed())

            // emits sort order reversed = false
            sortOrderReversedEmitter!!.onNext(false)

            subscriber.assertValueAt(3, result2)

            // emits sort order 1
            sortOrderEmitter!!.onNext(sortOrder1)

            subscriber.assertValueAt(4, result1)

            // emits sort order 2
            sortOrderEmitter!!.onNext(sortOrder2)

            subscriber.assertValueAt(5, result2)
        }
    }

    @Test
    fun test_getMediaListDuringSortOrderAndReverseFlowChanges_Failure() {
        // Testing flows of sort order change
        run {
            val subscriber = TestSubscriber.create<List<Media>>()

            /*mocking*/
            val sortOrder1 = "sort_order_1"
            val result1 = mockMediaList(size = 10)

            whenever(getSectionedMediaUseCase.getSortedCollection(eq(sortOrder1)))
                    .thenReturn(Flowable.just(result1))

            var sortOrderEmitter: Emitter<String>? = null

            whenever(preferences.getSortOrderForSection(eq(section)))
                    .thenReturn(
                            Flowable.create(
                                    { sortOrderEmitter = it },
                                    BackpressureStrategy.BUFFER
                            )
                    )

            whenever(preferences.isSortOrderReversedForSection(eq(section)))
                    .thenReturn(Flowable.just(false))

            /*calling method*/
            getSectionedMediaUseCase.getMediaList()
                    .subscribe(subscriber)

            /*testing sort order 1*/
            sortOrderEmitter!!.onNext(sortOrder1)
            subscriber.assertValueAt(0, result1)

            /*testing sort order error*/
            sortOrderEmitter!!.onError(UnsupportedOperationException())
            subscriber.assertError(UnsupportedOperationException::class.java)
        }
    }

    @Test
    fun test_getMediaList_Failure() {
        run {
            val subscriber = TestSubscriber.create<List<Media>>()

            /*mocking*/
            val sortOrder = "sort_order"

            whenever(getSectionedMediaUseCase.getSortedCollection(eq(sortOrder)))
                    .thenReturn(Flowable.error(UnsupportedOperationException()))

            whenever(preferences.getSortOrderForSection(eq(section)))
                    .thenReturn(Flowable.just(sortOrder))

            whenever(preferences.isSortOrderReversedForSection(eq(section)))
                    .thenReturn(Flowable.just(true))

            /*calling method*/
            getSectionedMediaUseCase.getMediaList()
                    .subscribe(subscriber)

            /*testing*/
            subscriber.assertError(UnsupportedOperationException::class.java)
        }
    }

    @Test
    fun test_getSortOrderMenu_Success() {
        run {
            val observer = TestObserver.create<SortOrderMenu>()

            /*mocking*/
            val sortOrders = listOf(
                TestSortOrder("sort_order_1"),
                TestSortOrder("sort_order_2"),
                TestSortOrder("sort_order_3")
            )

            val currSortOrder = sortOrders.first()
            val reversed = true

            val testMenu = SortOrderMenu(
                    sortOrders,
                    currSortOrder,
                    reversed
            )

            whenever(repository.sortOrders)
                    .thenReturn(Single.just(sortOrders))

            whenever(preferences.getSortOrderForSection(eq(section)))
                    .thenReturn(Flowable.just(currSortOrder.key))

            whenever(preferences.isSortOrderReversedForSection(eq(section)))
                    .thenReturn(Flowable.just(reversed))

            /*calling method*/
            getSectionedMediaUseCase.getSortOrderMenu()
                    .subscribe(observer)

            /*testing*/
            observer.assertResult(
                    testMenu
            )
        }
    }

    @Test
    fun test_getSortOrderMenu_Failure() {
        run {
            val observer = TestObserver.create<SortOrderMenu>()

            /*mocking*/
            val sortOrders = mapOf(
                    "sort_order_1" to "sort_order_name_1"
            )

            val currSortOrder = sortOrders.entries.first().key
            val reversed = false

            whenever(repository.sortOrders)
                    .thenReturn(Single.error(UnsupportedOperationException()))

            whenever(preferences.getSortOrderForSection(eq(section)))
                    .thenReturn(Flowable.just(currSortOrder))

            whenever(preferences.isSortOrderReversedForSection(eq(section)))
                    .thenReturn(Flowable.just(reversed))

            /*calling method*/
            getSectionedMediaUseCase.getSortOrderMenu()
                    .subscribe(observer)

            /*testing*/
            observer.assertError(
                    UnsupportedOperationException::class.java
            )
        }
    }

    @Test
    fun test_applySortOrder_Success() {
        run {
            val observer = TestObserver.create<List<Media>>()

            val targetSortOrder = TestSortOrder("sort_order_1")

            whenever(preferences.saveSortOrderForSection(eq(section), eq(targetSortOrder.key)))
                    .doReturn(Completable.complete())

            /*calling method*/
            getSectionedMediaUseCase.applySortOrder(targetSortOrder)
                    .subscribe(observer)

            /*testing*/
            observer.assertComplete()
        }
    }

    @Test
    fun test_applySortOrder_Failure() {
        run {
            val observer = TestObserver.create<List<Media>>()

            val targetSortOrder = TestSortOrder("sort_order_1")

            whenever(preferences.saveSortOrderForSection(eq(section), eq(targetSortOrder.key)))
                    .doReturn(Completable.error(UnsupportedOperationException()))

            /*calling method*/
            getSectionedMediaUseCase.applySortOrder(targetSortOrder)
                    .subscribe(observer)

            /*testing*/
            observer.assertError(UnsupportedOperationException::class.java)
        }
    }

    @Test
    fun test_applySortOrderReversed_Success() {
        run {
            val observer = TestObserver.create<List<Media>>()

            val targetReversed = true

            whenever(preferences.saveSortOrderReversedForSection(eq(section), eq(targetReversed)))
                    .doReturn(Completable.complete())

            /*calling method*/
            getSectionedMediaUseCase.applySortOrderReversed(targetReversed)
                    .subscribe(observer)

            /*testing*/
            observer.assertComplete()
        }
    }

    @Test
    fun test_applySortOrderReversed_Failure() {
        run {
            val observer = TestObserver.create<List<Media>>()

            val targetReversed = false

            whenever(preferences.saveSortOrderReversedForSection(eq(section), eq(targetReversed)))
                    .doReturn(Completable.error(java.lang.UnsupportedOperationException()))

            /*calling method*/
            getSectionedMediaUseCase.applySortOrderReversed(targetReversed)
                    .subscribe(observer)

            /*testing*/
            observer.assertError(UnsupportedOperationException::class.java)
        }
    }

}