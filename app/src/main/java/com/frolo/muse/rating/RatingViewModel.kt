package com.frolo.muse.rating

import androidx.lifecycle.LiveData
import com.frolo.muse.arch.SingleLiveEvent
import com.frolo.muse.arch.call
import com.frolo.muse.interactor.rate.RatingUseCase
import com.frolo.muse.logger.*
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseViewModel
import io.reactivex.disposables.Disposable
import javax.inject.Inject


class RatingViewModel @Inject constructor(
    private val ratingUseCase: RatingUseCase,
    private val schedulerProvider: SchedulerProvider,
    private val eventLogger: EventLogger
): BaseViewModel(eventLogger) {

    private var ratingDisposable: Disposable? = null

    private val _ratingEvent = SingleLiveEvent<Unit>()
    val ratingEvent: LiveData<Unit> get() = _ratingEvent

    fun onResume() {
        ratingUseCase
            .isRatingRequested()
            .observeOn(schedulerProvider.main())
            .doOnSubscribe { d ->
                ratingDisposable?.dispose()
                ratingDisposable = d
            }
            .subscribeFor { flag ->
                if (flag) {
                    _ratingEvent.call()
                }
            }
    }

    fun onPause() {
        ratingDisposable?.dispose()
        ratingDisposable = null
    }

    fun onPositiveAnswer() {
        ratingUseCase.positiveAnswer()
            .observeOn(schedulerProvider.main())
            .doOnComplete {
                eventLogger.logRateDialogAnswered(RATE_DIALOG_ANSWER_YES)
            }
            .subscribeFor(key = "positive_answer") { }
    }

    fun onNegativeAnswer() {
        ratingUseCase.negativeAnswer()
            .observeOn(schedulerProvider.main())
            .doOnComplete {
                eventLogger.logRateDialogAnswered(RATE_DIALOG_ANSWER_NO)
            }
            .subscribeFor(key = "negative_answer") { }
    }

    fun onNeutralAnswer() {
        ratingUseCase.neutralAnswer()
            .observeOn(schedulerProvider.main())
            .doOnComplete {
                eventLogger.logRateDialogAnswered(RATE_DIALOG_ANSWER_REMIND_LATER)
            }
            .subscribeFor(key = "neutral_answer") { }
    }

    fun onCancel() {
        ratingUseCase.cancel()
            .observeOn(schedulerProvider.main())
            .doOnComplete {
                eventLogger.logRateDialogCancelled()
            }
            .subscribeFor(key = "cancel") { }
    }
}