package com.frolo.muse.ui.main.settings.crossfade

import android.app.Dialog
import android.os.Bundle
import androidx.core.math.MathUtils
import androidx.lifecycle.LifecycleOwner
import com.frolo.muse.R
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.ui.base.BaseDialogFragment
import kotlinx.android.synthetic.main.dialog_cross_fade.*


class CrossFadeDialog : BaseDialogFragment() {

    private val viewModel: CrossFadeViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeViewModel(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setContentView(R.layout.dialog_cross_fade)
            setupDialogSizeByDefault(this)
            loadUI(this)
        }
    }

    private fun loadUI(dialog: Dialog) = with(dialog) {

        chb_enable_cross_fade.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.onEnableCrossFadeChecked()
            } else {
                viewModel.onEnableCrossFadeUnchecked()
            }
        }

        slider_cross_fade_duration.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                viewModel.onChangedCrossFadeDuration(value.toInt())
            }
        }

        btn_save.setOnClickListener {
            dismiss()
        }

    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        crossFadeEnabled.observeNonNull(owner) { enabled ->
            dialog?.apply {
                chb_enable_cross_fade.isChecked = enabled

                tv_cross_fade_duration_desc.alpha = if (enabled) 1f else 0.5f
                slider_cross_fade_duration.isEnabled = enabled
            }
        }

        crossFadeDurationRange.observeNonNull(owner) { range ->
            dialog?.apply {
                slider_cross_fade_duration.valueFrom = range.min
                slider_cross_fade_duration.valueTo = range.max
            }
        }

        crossFadeDuration.observeNonNull(owner) { value ->
            dialog?.apply {
                val min = slider_cross_fade_duration.valueFrom
                val max = slider_cross_fade_duration.valueTo
                slider_cross_fade_duration.value = MathUtils.clamp(value, min, max)
            }
        }
    }

    companion object {

        fun newInstance() = CrossFadeDialog()

    }

}