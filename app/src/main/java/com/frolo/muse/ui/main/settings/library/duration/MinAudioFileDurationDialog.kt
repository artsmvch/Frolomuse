package com.frolo.muse.ui.main.settings.library.duration

import android.app.Dialog
import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import com.frolo.muse.R
import com.frolo.muse.arch.observe
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.ui.base.BaseDialogFragment
import com.frolo.muse.ui.main.settings.limitNumberInput
import com.frolo.muse.ui.main.settings.updateText
import kotlinx.android.synthetic.main.dialog_min_audio_file_duration.*


class MinAudioFileDurationDialog : BaseDialogFragment() {

    private val viewModel: MinAudioFileDurationViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeViewModel(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setContentView(R.layout.dialog_min_audio_file_duration)
            setupDialogSizeByDefault(this)
            loadUI(this)
        }
    }

    private fun loadUI(dialog: Dialog) = with(dialog) {
        edt_minutes.limitNumberInput(9)

        edt_seconds.limitNumberInput(59)

        btn_cancel.setOnClickListener {
            viewModel.onCancelClicked()
        }

        btn_save.setOnClickListener {
            val typedMinutes = edt_minutes.text?.toString()?.toIntOrNull() ?: 0
            val typedSeconds = edt_seconds.text?.toString()?.toIntOrNull() ?: 0
            viewModel.onSaveClicked(
                typedMinutes = typedMinutes,
                typedSeconds = typedSeconds
            )
        }
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        minutes.observe(owner) {
            dialog?.apply {
                edt_minutes.updateText(it?.toString())
            }
        }

        seconds.observe(owner) {
            dialog?.apply {
                edt_seconds.updateText(it?.toString())
            }
        }

        goBackEvent.observe(owner) {
            dismiss()
        }

        error.observeNonNull(owner) {
            postError(it)
        }
    }

    companion object {

        fun newInstance() = MinAudioFileDurationDialog()

    }

}