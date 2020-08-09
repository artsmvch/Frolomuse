package com.frolo.muse.ui.main.settings.playback

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.arch.combine
import com.frolo.muse.interactor.player.PlaybackFadingUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.logPlaybackFadingConfigured
import com.frolo.muse.model.FloatRange
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseViewModel
import javax.inject.Inject


class PlaybackFadingViewModel @Inject constructor(
    private val playbackFadingUseCase: PlaybackFadingUseCase,
    private val schedulerProvider: SchedulerProvider,
    private val eventLogger: EventLogger
): BaseViewModel(eventLogger) {

    val playbackFadingDurationRange: LiveData<FloatRange> by lazy {
        MutableLiveData<FloatRange>().apply {
            playbackFadingUseCase.getPlaybackFadingDurationRange()
                .observeOn(schedulerProvider.main())
                .subscribeFor { value = it }
        }
    }

    private val _playbackFadingDuration = MutableLiveData<Float>(0f).apply {
        playbackFadingUseCase.getCurrentPlaybackFadingParams()
            .observeOn(schedulerProvider.main())
            .subscribeFor { params -> value = params.interval / 1000f }
    }
    val playbackFadingDuration: LiveData<Float> =
        combine(playbackFadingDurationRange, _playbackFadingDuration) { range, duration ->
            when {
                range == null -> 0f
                duration == null -> 0f
                else -> duration.coerceIn(range.min, range.max)
            }
        }

    fun onChangedPlaybackFadingDuration(seconds: Int) {
        _playbackFadingDuration.value = seconds.toFloat()
        val milliseconds = seconds * 1000
        playbackFadingUseCase
            .applyPlaybackFadingDuration(milliseconds)
            .observeOn(schedulerProvider.main())
            .subscribeFor {  }
    }

    fun onStoppedChangingPlaybackFadingDuration() {
        val seconds = playbackFadingDuration.value ?: return
        val milliseconds = (seconds * 1000).toInt()
        playbackFadingUseCase
            .applyAndSavePlaybackFadingDuration(milliseconds)
            .observeOn(schedulerProvider.main())
            .doOnComplete { eventLogger.logPlaybackFadingConfigured(seconds.toInt()) }
            .subscribeFor {  }
    }

}