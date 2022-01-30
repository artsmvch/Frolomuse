package com.frolo.equalizerview.impl

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.UiThread
import com.frolo.equalizerview.BaseEqualizerView
import com.frolo.equalizerview.R
import com.frolo.rx.KeyedDisposableContainer
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
    private val levelDispatcherDisposables = KeyedDisposableContainer<Int>()

    private fun getBandLevelProcessor(bandIndex: Int): FlowableProcessor<Int>? {
        if (!isAttachedToWindow) {
            // If the view is not attached to window, then we don't provide
            // a flowable processor for band level dispatching. This way,
            // we can control the resources and be sure, that the rx sources
            // don't hold references to this view when it is detached,
            // so there are no possible memory leaks.
            return null
        }
        return bandLevelProcessors.getOrPut(bandIndex) {
            PublishProcessor.create<Int>().also { processor ->
                processor
                    .onBackpressureLatest()
                    .throttleLast(SET_BAND_LEVEL_THROTTLING_IN_MS, TimeUnit.MILLISECONDS)
                    .observeOn(levelDispatcherScheduler)
                    .subscribe { bandLevel ->
                        safelySetBandLevel(bandIndex, bandLevel)
                    }
                    .also { disposable ->
                        levelDispatcherDisposables.add(bandIndex, disposable)
                    }
            }

        }
    }

    @UiThread
    private fun safelySetBandLevel(bandIndex: Int, bandLevel: Int) {
        val safeEqualizer = this.equalizer ?: return
        val numberOfBands = safeEqualizer.numberOfBands
        if (bandIndex in 0 until numberOfBands) {
            safeEqualizer.setBandLevel(bandIndex.toShort(), bandLevel.toShort())
        }
    }

    override fun onCreateBandView(): SeekBarBandView {
        return SeekBarBandView(context)
    }

    override fun onDispatchLevelChange(bandIndex: Int, level: Int) {
        getBandLevelProcessor(bandIndex)?.onNext(level)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        clearResources()
    }

    private fun clearResources() {
        bandLevelProcessors.values.forEach { it.onComplete() }
        bandLevelProcessors.clear()
        levelDispatcherDisposables.clear()
    }

    companion object {
        private const val SET_BAND_LEVEL_THROTTLING_IN_MS = 350L
    }

}