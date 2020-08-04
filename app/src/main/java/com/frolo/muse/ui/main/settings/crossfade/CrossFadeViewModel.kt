package com.frolo.muse.ui.main.settings.crossfade

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.arch.liveDataOf
import com.frolo.muse.engine.CrossFadeStrategy
import com.frolo.muse.engine.Player
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.FloatRange
import com.frolo.muse.ui.base.BaseViewModel
import javax.inject.Inject


// TODO: remake this bullshit
class CrossFadeViewModel @Inject constructor(
    private val player: Player,
    private val eventLogger: EventLogger
): BaseViewModel(eventLogger) {

    companion object {

        @Deprecated(message = "That's wrong")
        private var crossFadeStrategy: CrossFadeStrategy = CrossFadeStrategy.withSmartStaticInterval(5_000)

    }

    private val _crossFadeEnabled = MutableLiveData<Boolean>().apply {
        value = player.getCrossFadeStrategy() != null
    }
    val crossFadeEnabled: LiveData<Boolean> get() = _crossFadeEnabled

    val crossFadeDurationRange: LiveData<FloatRange> = liveDataOf(FloatRange.of(0f, 30f))

    private val _crossFadeDuration = MutableLiveData<Float>(0f).apply {
        value = CrossFadeStrategy.getInterval(crossFadeStrategy).toFloat() / 1000
    }
    val crossFadeDuration: LiveData<Float> get() = _crossFadeDuration

    fun onEnableCrossFadeChecked() {
        _crossFadeEnabled.value = true
        player.setCrossFadeStrategy(crossFadeStrategy)
    }

    fun onEnableCrossFadeUnchecked() {
        _crossFadeEnabled.value = false
        player.setCrossFadeStrategy(null)
    }

    fun onChangedCrossFadeDuration(seconds: Int) {
        val milliseconds = seconds * 1000
        crossFadeStrategy = CrossFadeStrategy.withSmartStaticInterval(milliseconds).also {
            player.setCrossFadeStrategy(it)
            _crossFadeDuration.value = seconds.toFloat()
        }
    }

}