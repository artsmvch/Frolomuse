package com.frolo.muse.ui.main.settings.playback

import android.app.Dialog
import android.os.Bundle
import androidx.core.math.MathUtils
import androidx.lifecycle.LifecycleOwner
import com.frolo.muse.R
import com.frolo.arch.support.observeNonNull
import com.frolo.muse.ui.base.BaseDialogFragment
import com.google.android.material.slider.Slider
import kotlinx.android.synthetic.main.dialog_playback_fading.*


class PlaybackFadingDialog : BaseDialogFragment() {

    private val viewModel: PlaybackFadingViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeViewModel(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setContentView(R.layout.dialog_playback_fading)
            setupDialogSizeByDefault(this)
            loadUI(this)
        }
    }

    private fun loadUI(dialog: Dialog) = with(dialog) {

        slider_playback_fading_duration.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                viewModel.onChangedPlaybackFadingDuration(value.toInt())
            }
        }

        slider_playback_fading_duration.addOnSliderTouchListener(
            object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) = Unit

                override fun onStopTrackingTouch(slider: Slider) {
                    viewModel.onStoppedChangingPlaybackFadingDuration()
                }
            }
        )

        btn_save.setOnClickListener {
            dismiss()
        }

    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {

        playbackFadingDurationRange.observeNonNull(owner) { range ->
            dialog?.apply {
                slider_playback_fading_duration.valueFrom = range.min
                slider_playback_fading_duration.valueTo = range.max
            }
        }

        playbackFadingDuration.observeNonNull(owner) { value ->
            dialog?.apply {
                val min = slider_playback_fading_duration.valueFrom
                val max = slider_playback_fading_duration.valueTo
                slider_playback_fading_duration.value = MathUtils.clamp(value, min, max)
            }
        }

    }

    companion object {

        fun newInstance() = PlaybackFadingDialog()

    }

}