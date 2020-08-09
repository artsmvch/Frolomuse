package com.frolo.muse.ui.main.settings.crossfade

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.arch.combine
import com.frolo.muse.interactor.player.CrossFadeUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.logCrossFadeConfigured
import com.frolo.muse.model.FloatRange
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseViewModel
import javax.inject.Inject


// TODO: remake this bullshit
class CrossFadeViewModel @Inject constructor(
    private val crossFadeUseCase: CrossFadeUseCase,
    private val schedulerProvider: SchedulerProvider,
    private val eventLogger: EventLogger
): BaseViewModel(eventLogger) {

    val crossFadeDurationRange: LiveData<FloatRange> by lazy {
        MutableLiveData<FloatRange>().apply {
            crossFadeUseCase.getCrossFadeRange()
                .observeOn(schedulerProvider.main())
                .subscribeFor { value = it }
        }
    }

    private val _crossFadeDuration = MutableLiveData<Float>(0f).apply {
        crossFadeUseCase.getCurrentCrossFadeParams()
            .observeOn(schedulerProvider.main())
            .subscribeFor { params -> value = params.interval / 1000f }
    }
    val crossFadeDuration: LiveData<Float> =
        combine(crossFadeDurationRange, _crossFadeDuration) { range, duration ->
            when {
                range == null -> 0f
                duration == null -> 0f
                else -> duration.coerceIn(range.min, range.max)
            }
        }

    fun onChangedCrossFadeDuration(seconds: Int) {
        _crossFadeDuration.value = seconds.toFloat()
        val milliseconds = seconds * 1000
        crossFadeUseCase
            .applyCrossFadeDuration(milliseconds)
            .observeOn(schedulerProvider.main())
            .subscribeFor {  }
    }

    fun onStoppedChangingCrossFadeDuration() {
        val seconds = crossFadeDuration.value ?: return
        val milliseconds = (seconds * 1000).toInt()
        crossFadeUseCase
            .applyAndSaveCrossFadeDuration(milliseconds)
            .observeOn(schedulerProvider.main())
            .doOnComplete { eventLogger.logCrossFadeConfigured(seconds.toInt()) }
            .subscribeFor {  }
    }

}