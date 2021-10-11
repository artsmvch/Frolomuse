package com.frolo.muse.ui.main.audiofx.customview.impl

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.UiThread
import com.frolo.muse.R
import com.frolo.muse.rx.KeyedDisposableContainer
import com.frolo.muse.ui.main.audiofx.customview.BaseEqualizerView
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import java.util.concurrent.TimeUnit


/**
 * Equalizer view based on [SeekBarBandView].
 */
class SeekBarEqualizerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.equalizerViewStyle
): BaseEqualizerView<SeekBarBandView>(context, attrs, defStyleAttr) {

    private val levelDispatcherScheduler: Scheduler get() = AndroidSchedulers.mainThread()
    private val bandLevelProcessors = HashMap<Int, FlowableProcessor<Int>>()
    private val internalDisposables = KeyedDisposableContainer<Int>()

    private fun getBandLevelProcessor(bandIndex: Int): FlowableProcessor<Int> {
        return bandLevelProcessors.getOrPut(bandIndex) {
            PublishProcessor.create<Int>().also { processor ->
                processor.throttleLast(SET_BAND_LEVEL_THROTTLING_IN_MS, TimeUnit.MILLISECONDS)
                    .observeOn(levelDispatcherScheduler)
                    .onBackpressureLatest()
                    .subscribe { bandLevel ->
                        safelySetBandLevel(bandIndex, bandLevel)
                    }
                    .also { disposable ->
                        internalDisposables.add(bandIndex, disposable)
                    }
            }

        }
    }

    @UiThread
    private fun safelySetBandLevel(bandIndex: Int, bandLevel: Int) {
        val safeAudioFx = this.audioFx ?: return
        val numberOfBands = safeAudioFx.numberOfBands.toInt()
        if (bandIndex in 0 until numberOfBands) {
            safeAudioFx.setBandLevel(bandIndex.toShort(), bandLevel.toShort())
        }
    }

    override fun onCreateBandView(): SeekBarBandView {
        return SeekBarBandView(context)
    }

    override fun onDispatchLevelChange(bandIndex: Int, level: Int) {
        getBandLevelProcessor(bandIndex).onNext(level)
    }

    companion object {
        private const val SET_BAND_LEVEL_THROTTLING_IN_MS = 350L
    }

}