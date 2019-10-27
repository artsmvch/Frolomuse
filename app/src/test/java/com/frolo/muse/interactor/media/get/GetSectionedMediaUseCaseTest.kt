package com.frolo.muse.interactor.media.get

import com.frolo.muse.TestSchedulerProvider
import com.frolo.muse.mockMediaList
import com.frolo.muse.model.Library
import com.frolo.muse.model.media.Media
import com.frolo.muse.model.menu.SortOrderMenu
import com.frolo.muse.repository.MediaRepository
import com.frolo.muse.repository.Preferences
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.thenDoNothing
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
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
                    .thenReturn(sortOrder)

            whenever(preferences.isSortOrderReversedForSection(eq(section)))
                    .thenReturn(true)

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
                    .thenReturn(sortOrder)

            whenever(preferences.isSortOrderReversedForSection(eq(section)))
                    .thenReturn(false)

            /*calling method*/
            getSectionedMediaUseCase.getMediaList()
                    .subscribe(subscriber)

            /*testing*/
            subscriber.assertResult(result)
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
                    .thenReturn(sortOrder)

            whenever(preferences.isSortOrderReversedForSection(eq(section)))
                    .thenReturn(true)

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
            val sortOrders = mapOf(
                    "sort_order_1" to "sort_order_name_1",
                    "sort_order_2" to "sort_order_name_2",
                    "sort_order_3" to "sort_order_name_3"
            )

            val currSortOrder = sortOrders.entries.first().key
            val reversed = true

            val testMenu = SortOrderMenu(
                    sortOrders,
                    currSortOrder,
                    reversed
            )

            whenever(repository.sortOrders)
                    .thenReturn(Single.just(sortOrders))

            whenever(preferences.getSortOrderForSection(eq(section)))
                    .thenReturn(currSortOrder)

            whenever(preferences.isSortOrderReversedForSection(eq(section)))
                    .thenReturn(reversed)

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
                    .thenReturn(currSortOrder)

            whenever(preferences.isSortOrderReversedForSection(eq(section)))
                    .thenReturn(reversed)

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
            val subscriber = TestSubscriber.create<List<Media>>()

            /*mocking*/
            val sortOrders = mapOf(
                    "sort_order_1" to "sort_order_name_1",
                    "sort_order_2" to "sort_order_name_2"
            )

            val reversed = false

            val targetSortOrder = sortOrders.entries.iterator().let {
                it.next()
                it.next().let { entry ->
                    entry.key
                }
            }

            val result = mockMediaList(size = 10)

            whenever(repository.sortOrders)
                    .thenReturn(Single.just(sortOrders))

            whenever(preferences.saveSortOrderForSection(eq(section), eq(targetSortOrder)))
                    .thenDoNothing()

            whenever(preferences.getSortOrderForSection(eq(section)))
                    .thenReturn(targetSortOrder)

            whenever(preferences.isSortOrderReversedForSection(eq(section)))
                    .thenReturn(reversed)

            whenever(getSectionedMediaUseCase.getSortedCollection(eq(targetSortOrder)))
                    .thenReturn(Flowable.just(result))

            /*calling method*/
            getSectionedMediaUseCase.applySortOrder(targetSortOrder)
                    .subscribe(subscriber)

            /*testing*/
            subscriber.assertResult(result)
        }
    }

    @Test
    fun test_applySortOrder_Failure() {
        run {
            val subscriber = TestSubscriber.create<List<Media>>()

            /*mocking*/
            val sortOrders = mapOf(
                    "sort_order_1" to "sort_order_name_1",
                    "sort_order_2" to "sort_order_name_2"
            )

            val reversed = false

            val targetSortOrder = sortOrders.entries.iterator().let {
                it.next()
                it.next().let { entry ->
                    entry.key
                }
            }

            val result = mockMediaList(size = 10)

            whenever(preferences.saveSortOrderForSection(eq(section), eq(targetSortOrder)))
                    .thenDoNothing()

            whenever(preferences.getSortOrderForSection(eq(section)))
                    .thenReturn(targetSortOrder)

            whenever(preferences.isSortOrderReversedForSection(eq(section)))
                    .thenReturn(reversed)

            whenever(getSectionedMediaUseCase.getSortedCollection(eq(targetSortOrder)))
                    .thenReturn(Flowable.error(UnsupportedOperationException()))

            /*calling method*/
            getSectionedMediaUseCase.applySortOrder(targetSortOrder)
                    .subscribe(subscriber)

            /*testing*/
            subscriber.assertError(UnsupportedOperationException::class.java)
        }
    }

    @Test
    fun test_applySortOrderReversed_Success() {
        run {
            val subscriber = TestSubscriber.create<List<Media>>()

            /*mocking*/
            val sortOrders = mapOf(
                    "sort_order_1" to "sort_order_name_1",
                    "sort_order_2" to "sort_order_name_2"
            )

            val currSortOrder = sortOrders.entries.first().key

            val targetReversed = true

            val result = mockMediaList(size = 10)

            whenever(repository.sortOrders)
                    .thenReturn(Single.just(sortOrders))

            whenever(preferences.saveSortOrderReversedForSection(eq(section), eq(targetReversed)))
                    .thenDoNothing()

            whenever(preferences.getSortOrderForSection(eq(section)))
                    .thenReturn(currSortOrder)

            whenever(preferences.isSortOrderReversedForSection(eq(section)))
                    .thenReturn(targetReversed)

            whenever(getSectionedMediaUseCase.getSortedCollection(eq(currSortOrder)))
                    .thenReturn(Flowable.just(result))

            /*calling method*/
            getSectionedMediaUseCase.applySortOrderReversed(targetReversed)
                    .subscribe(subscriber)

            /*testing*/
            subscriber.assertResult(
                    result.reversed() // targetReversed = true
            )
        }
    }

    @Test
    fun test_applySortOrderReversed_Failure() {
        run {
            val subscriber = TestSubscriber.create<List<Media>>()

            /*mocking*/
            val sortOrders = mapOf(
                    "sort_order_1" to "sort_order_name_1",
                    "sort_order_2" to "sort_order_name_2"
            )

            val currSortOrder = sortOrders.entries.first().key

            val targetReversed = false

            val result = mockMediaList(size = 10)

            whenever(preferences.saveSortOrderReversedForSection(eq(section), eq(targetReversed)))
                    .thenDoNothing()

            whenever(preferences.getSortOrderForSection(eq(section)))
                    .thenReturn(currSortOrder)

            whenever(preferences.isSortOrderReversedForSection(eq(section)))
                    .thenReturn(targetReversed)

            whenever(getSectionedMediaUseCase.getSortedCollection(eq(currSortOrder)))
                    .thenReturn(Flowable.error(UnsupportedOperationException()))

            /*calling method*/
            getSectionedMediaUseCase.applySortOrderReversed(targetReversed)
                    .subscribe(subscriber)

            /*testing*/
            subscriber.assertError(UnsupportedOperationException::class.java)
        }
    }

}