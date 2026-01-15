package com.frolo.muse.ui.main.library.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.router.AppRouter
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.favourite.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.favourite.GetIsFavouriteUseCase
import com.frolo.muse.interactor.media.get.SearchMediaUseCase
import com.frolo.muse.interactor.media.shortcut.CreateShortcutUseCase
import com.frolo.muse.audius.usecase.SearchAudiusTracksUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.logMediaSearchUsed
import com.frolo.music.model.Media
import com.frolo.muse.permission.PermissionChecker
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.main.library.base.AbsMediaCollectionViewModel
import io.reactivex.processors.PublishProcessor
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class SearchViewModel @Inject constructor(
    permissionChecker: PermissionChecker,
    searchMediaUseCase: SearchMediaUseCase,
    searchAudiusTracksUseCase: SearchAudiusTracksUseCase,
    getMediaMenuUseCase: GetMediaMenuUseCase<Media>,
    clickMediaUseCase: ClickMediaUseCase<Media>,
    playMediaUseCase: PlayMediaUseCase<Media>,
    shareMediaUseCase: ShareMediaUseCase<Media>,
    deleteMediaUseCase: DeleteMediaUseCase<Media>,
    getIsFavouriteUseCase: GetIsFavouriteUseCase<Media>,
    changeFavouriteUseCase: ChangeFavouriteUseCase<Media>,
    createShortcutUseCase: CreateShortcutUseCase<Media>,
    schedulerProvider: SchedulerProvider,
    appRouter: AppRouter,
    private val eventLogger: EventLogger
): AbsMediaCollectionViewModel<Media>(
    permissionChecker,
    searchMediaUseCase,
    getMediaMenuUseCase,
    clickMediaUseCase,
    playMediaUseCase,
    shareMediaUseCase,
    deleteMediaUseCase,
    getIsFavouriteUseCase,
    changeFavouriteUseCase,
    createShortcutUseCase,
    schedulerProvider,
    appRouter,
    eventLogger
) {

    private var queryCount: Int = 0

    private val publisher: PublishProcessor<String> by lazy {
        PublishProcessor.create<String>().also { publisher ->
            publisher.debounce(200L, TimeUnit.MILLISECONDS)
                .filter { query -> query.isNotEmpty() }
                .distinctUntilChanged()
                .flatMapSingle { query ->
                    val localSearch = searchMediaUseCase.search(query).firstOrError()
                    val audiusSearch = searchAudiusTracksUseCase.search(query)
                    
                    Single.zip(localSearch, audiusSearch) { localResults, audiusResults ->
                        val combinedResults = mutableListOf<Media>()
                        combinedResults.addAll(localResults)
                        combinedResults.addAll(audiusResults)
                        query to combinedResults
                    }
                }
                .observeOn(schedulerProvider.main())
                .subscribeFor { pair ->
                    queryCount++
                    _query.value = pair.first
                    submitMediaList(pair.second)
                }
        }
    }

    private val _query: MutableLiveData<String> = MutableLiveData()
    val query: LiveData<String> get() = _query

    fun onQuerySubmitted(query: String) {
        publisher.onNext(query)
    }

    override fun onStart() {
        super.onStart()

        // Every time the view model starts, [queryCount] is reset to 0.
        queryCount = 0
    }

    override fun onStop() {
        super.onStop()

        queryCount.also { count ->
            if (count > 0) eventLogger.logMediaSearchUsed(queryCount = count)
        }
    }

}