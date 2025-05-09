package com.frolo.muse.ui.main.settings.playback

import android.app.Dialog
import android.os.Bundle
import androidx.core.math.MathUtils
import androidx.lifecycle.LifecycleOwner
import com.frolo.arch.support.observeNonNull
import com.frolo.muse.databinding.DialogPlaybackFadingBinding
import com.frolo.muse.ui.base.BaseDialogFragment
import com.google.android.material.slider.Slider


class PlaybackFadingDialog : BaseDialogFragment() {
    private var _binding: DialogPlaybackFadingBinding? = null
    private val binding: DialogPlaybackFadingBinding get() = _binding!!

    private val viewModel: PlaybackFadingViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeViewModel(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            _binding = DialogPlaybackFadingBinding.inflate(layoutInflater)
            setContentView(binding.root)
            setupDialogSizeByDefault(this)
            loadUi(this)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadUi(dialog: Dialog) = with(binding) {
        sliderPlaybackFadingDuration.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                viewModel.onChangedPlaybackFadingDuration(value.toInt())
            }
        }

        sliderPlaybackFadingDuration.addOnSliderTouchListener(
            object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) = Unit

                override fun onStopTrackingTouch(slider: Slider) {
                    viewModel.onStoppedChangingPlaybackFadingDuration()
                }
            }
        )

        btnSave.setOnClickListener {
            dismiss()
        }
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {

        playbackFadingDurationRange.observeNonNull(owner) { range ->
            dialog?.apply {
                binding.sliderPlaybackFadingDuration.apply {
                    valueFrom = range.min
                    valueTo = range.max
                }
            }
        }

        playbackFadingDuration.observeNonNull(owner) { value ->
            dialog?.apply {
                binding.sliderPlaybackFadingDuration.apply {
                    this.value = MathUtils.clamp(value, valueFrom, valueTo)
                }
            }
        }

    }

    companion object {

        fun newInstance() = PlaybackFadingDialog()

    }

}