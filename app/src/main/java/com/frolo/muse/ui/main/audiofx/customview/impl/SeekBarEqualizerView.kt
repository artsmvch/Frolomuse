package com.frolo.muse.ui.main.audiofx.customview.impl

import android.content.Context
import android.util.AttributeSet
import com.frolo.muse.R
import com.frolo.muse.ui.main.audiofx.customview.BaseEqualizerView


/**
 * Equalizer view based on [SeekBarBandView].
 */
class SeekBarEqualizerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.equalizerViewStyle
): BaseEqualizerView<SeekBarBandView>(context, attrs, defStyleAttr) {

    override fun onCreateBandView(): SeekBarBandView {
        return SeekBarBandView(context)
    }

}