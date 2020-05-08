package com.frolo.muse.ui.main.library.base

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.frolo.muse.*
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.favourite.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.favourite.GetIsFavouriteUseCase
import com.frolo.muse.interactor.media.get.GetMediaUseCase
import com.frolo.muse.interactor.media.shortcut.CreateShortcutUseCase
import com.frolo.muse.model.media.Media
import com.frolo.muse.model.menu.ContextualMenu
import com.frolo.muse.navigator.TestNavigator
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.MockitoAnnotations


@RunWith(JUnit4::class)
class AbsMediaCollectionViewModelTest {

    @get:Rule
    var instantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    @get:Rule
    val immediateSchedulerRule = ImmediateSchedulerRule()

    @Mock
    private lateinit var getMediaUseCase: GetMediaUseCase<Media>
    @Mock
    private lateinit var getMediaMenuUseCase: GetMediaMenuUseCase<Media>
    @Mock
    private lateinit var clickMediaUseCase: ClickMediaUseCase<Media>
    @Mock
    private lateinit var playMediaUseCase: PlayMediaUseCase<Media>
    @Mock
    private lateinit var shareMediaUseCase: ShareMediaUseCase<Media>
    @Mock
    private lateinit var deleteMediaUseCase: DeleteMediaUseCase<Media>
    @Mock
    private lateinit var getIsFavouriteUseCase: GetIsFavouriteUseCase<Media>
    @Mock
    private lateinit var changeFavouriteUseCase: ChangeFavouriteUseCase<Media>
    @Mock
    private lateinit var createShortcutUseCase: CreateShortcutUseCase<Media>
    private val schedulerProvider = TestSchedulerProvider.SHARED
    private val navigator = TestNavigator()
    private val eventLogger = TestEventLogger()

    private lateinit var viewModel: AbsMediaCollectionViewModel<Media>

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        val navigator = TestNavigator()
        val eventLogger = TestEventLogger()
        viewModel = TestMediaCollectionViewModel(
                getMediaUseCase,
                getMediaMenuUseCase,
                clickMediaUseCase,
                playMediaUseCase,
                shareMediaUseCase,
                deleteMediaUseCase,
                getIsFavouriteUseCase,
                changeFavouriteUseCase,
                createShortcutUseCase,
                schedulerProvider,
                navigator,
                eventLogger)

        viewModel.observeEntirely()
    }

    @Test
    fun test_fetchMediaList_Success() {
        whenever(getMediaUseCase.getMediaList())
                .doReturn(Flowable.just(mockMediaList(size = 10, allowIdCollisions = false)))

        viewModel.onActive()

        viewModel.mediaList.value.let { value ->
            assert(value != null)
            assert(value!!.size == 10)
        }

        viewModel.mediaItemCount.value.let { value ->
            assert(value == 10)
        }

        viewModel.error.value.let { value ->
            assert(value == null)
        }

        viewModel.placeholderVisible.value.let { value ->
            assert(value == false)
        }

        viewModel.isLoading.value.let { value ->
            assert(value == false)
        }
    }

    @Test
    fun test_fetchMediaItems_Failure() {
        whenever(getMediaUseCase.getMediaList())
                .doReturn(Flowable.error(RuntimeException()))

        viewModel.onActive()

        viewModel.mediaList.value.let { value ->
            assert(value.isNullOrEmpty())
        }

        viewModel.mediaItemCount.value.let { value ->
            assert(value == null || value == 0)
        }

        viewModel.error.value.let { value ->
            assert(value != null)
            assert(value is RuntimeException)
        }

        viewModel.placeholderVisible.value.let { value ->
            assert(value == true)
        }

        viewModel.isLoading.value.let { value ->
            assert(value == false)
        }
    }

    @Test
    fun test_fetchMediaList_PermissionNotGranted() {
        whenever(getMediaUseCase.getMediaList())
                .doReturn(Flowable.error(SecurityException()))

        viewModel.onActive()

        viewModel.mediaList.value.let { value ->
            assert(value.isNullOrEmpty())
        }

        viewModel.mediaItemCount.value.let { value ->
            assert(value == 0)
        }

        viewModel.error.value.let { value ->
            assert(value == null)
        }

        viewModel.placeholderVisible.value.let { value ->
            assert(value == true)
        }

        viewModel.isLoading.value.let { value ->
            assert(value == false)
        }

        viewModel.askReadPermissionEvent.value.let { value ->
            assert(value != null)
        }

        // After that
        whenever(getMediaUseCase.getMediaList())
                .doReturn(Flowable.just(mockMediaList(size = 10, allowIdCollisions = false)))

        // Assuming the user granted the permission
        viewModel.onReadPermissionGranted()

        viewModel.mediaList.value.let { value ->
            assert(value != null && value.size == 10)
        }

        viewModel.mediaItemCount.value.let { value ->
            assert(value == 10)
        }

        viewModel.error.value.let { value ->
            assert(value == null)
        }

        viewModel.placeholderVisible.value.let { value ->
            assert(value == false)
        }

        viewModel.isLoading.value.let { value ->
            assert(value == false)
        }
    }

    @Test
    fun test_fetchMediaItems_Empty() {
        whenever(getMediaUseCase.getMediaList())
                .doReturn(Flowable.just(mockMediaList(size = 0)))

        viewModel.onActive()

        viewModel.mediaList.value.let { value ->
            assert(value != null)
            assert(value!!.isEmpty())
        }

        viewModel.mediaItemCount.value.let { value ->
            assert(value == 0)
        }

        viewModel.error.value.let { value ->
            assert(value == null)
        }

        viewModel.placeholderVisible.value.let { value ->
            assert(value == true)
        }

        viewModel.isLoading.value.let { value ->
            assert(value == false)
        }
    }

    @Test
    fun test_contextualMenu() {
        val mockList = mockMediaList(size = 10)
        val mockMenu = ContextualMenu(
                mockList[0],
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true
                )

        whenever(getMediaUseCase.getMediaList())
                .doReturn(Flowable.just(mockList))

        whenever(getMediaMenuUseCase.getContextualMenu(eq(mockList[0])))
                .thenReturn(Single.just(mockMenu))

        viewModel.onActive()

        // After media list received

        viewModel.isInContextualMode.value.let { value ->
            assert(value != true)
        }

        viewModel.selectedItems.value.let { value ->
            assert(value.isNullOrEmpty())
        }

        viewModel.selectedItemsCount.value.let { value ->
            assert(value == 0)
        }

        val firstItem = viewModel.mediaList.value!![0]
        viewModel.onItemLongClicked(firstItem)

        // After an item has been long clicked

        viewModel.isInContextualMode.value.let { value ->
            assert(value == true)
        }

        viewModel.selectedItems.value.let { value ->
            assert(value!!.isNotEmpty())
            assert(value.count() == 1)
            assert(value.first() == firstItem)
        }

        viewModel.selectedItemsCount.value.let { value ->
            assert(value == 1)
        }

        viewModel.onItemClicked(firstItem)

        // After that item has been clicked again (Contextual menu should be closed)

        viewModel.isInContextualMode.value.let { value ->
            assert(value == false)
        }

        viewModel.selectedItems.value.let { value ->
            assert(value!!.isEmpty())
        }

        viewModel.selectedItemsCount.value.let { value ->
            assert(value == 0)
        }

        val secondItem = viewModel.mediaList.value!![1]
        viewModel.onItemLongClicked(firstItem)
        viewModel.onItemLongClicked(secondItem)

        // After the first 2 items long clicked

        viewModel.isInContextualMode.value.let { value ->
            assert(value == true)
        }

        viewModel.selectedItems.value.let { value ->
            assert(value!!.isNotEmpty())
            assert(value.count() == 2)
            val it = value.iterator()
            assert(it.next() == firstItem)
            assert(it.next() == secondItem)
        }

        viewModel.selectedItemsCount.value.let { value ->
            assert(value == 2)
        }

        viewModel.onContextualMenuClosed()

        // After the contextual menu closed

        viewModel.isInContextualMode.value.let { value ->
            assert(value == false)
        }

        viewModel.selectedItems.value.let { value ->
            assert(value!!.isEmpty())
        }

        viewModel.selectedItemsCount.value.let { value ->
            assert(value == 0)
        }

    }

}