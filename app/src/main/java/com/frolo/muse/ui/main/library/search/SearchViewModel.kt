package com.frolo.muse.ui.main.library.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.get.SearchMediaUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.media.Media
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.main.library.base.AbsMediaCollectionViewModel
import io.reactivex.processors.PublishProcessor
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class SearchViewModel @Inject constructor(
        searchMediaUseCase: SearchMediaUseCase,
        getMediaMenuUseCase: GetMediaMenuUseCase<Media>,
        clickMediaUseCase: ClickMediaUseCase<Media>,
        playMediaUseCase: PlayMediaUseCase<Media>,
        shareMediaUseCase: ShareMediaUseCase<Media>,
        deleteMediaUseCase: DeleteMediaUseCase<Media>,
        changeFavouriteUseCase: ChangeFavouriteUseCase<Media>,
        schedulerProvider: SchedulerProvider,
        navigator: Navigator,
        eventLogger: EventLogger
): AbsMediaCollectionViewModel<Media>(
        searchMediaUseCase,
        getMediaMenuUseCase,
        clickMediaUseCase,
        playMediaUseCase,
        shareMediaUseCase,
        deleteMediaUseCase,
        changeFavouriteUseCase,
        schedulerProvider,
        navigator,
        eventLogger) {

    private val publisher: PublishProcessor<String> by lazy {
        PublishProcessor.create<String>().also { publisher ->
            publisher.debounce(200, TimeUnit.MILLISECONDS)
                    .filter { query -> query.length >= 2 }
                    .switchMap { query ->
                        searchMediaUseCase.search(query)
                                .map { items -> query to items }
                    }
                    .observeOn(schedulerProvider.main())
                    .subscribeFor { pair ->
                        _query.value = pair.first
                        submitMediaList(pair.second)
                    }
        }
    }

    private val _query: MutableLiveData<String> = MutableLiveData()
    val query: LiveData<String> = _query

    fun onQuerySubmitted(query: String) {
        publisher.onNext(query)
    }
}