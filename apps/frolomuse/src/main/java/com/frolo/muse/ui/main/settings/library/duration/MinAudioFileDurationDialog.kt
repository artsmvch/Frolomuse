package com.frolo.muse.ui.main.settings.library.duration

import android.app.Dialog
import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import com.frolo.arch.support.observe
import com.frolo.arch.support.observeNonNull
import com.frolo.muse.databinding.DialogMinAudioFileDurationBinding
import com.frolo.muse.ui.base.BaseDialogFragment
import com.frolo.muse.ui.main.settings.limitNumberInput
import com.frolo.muse.ui.main.settings.updateText


class MinAudioFileDurationDialog : BaseDialogFragment() {
    private var _binding: DialogMinAudioFileDurationBinding? = null
    private val binding: DialogMinAudioFileDurationBinding get() = _binding!!

    private val viewModel: MinAudioFileDurationViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeViewModel(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            _binding = DialogMinAudioFileDurationBinding.inflate(layoutInflater)
            setContentView(binding.root)
            setupDialogSizeByDefault(this)
            loadUi()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadUi() = with(binding) {
        edtMinutes.limitNumberInput(9)

        edtSeconds.limitNumberInput(59)

        btnCancel.setOnClickListener {
            viewModel.onCancelClicked()
        }

        btnSave.setOnClickListener {
            val typedMinutes = edtMinutes.text?.toString()?.toIntOrNull() ?: 0
            val typedSeconds = edtSeconds.text?.toString()?.toIntOrNull() ?: 0
            viewModel.onSaveClicked(
                typedMinutes = typedMinutes,
                typedSeconds = typedSeconds
            )
        }
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        minutes.observe(owner) {
            dialog?.apply {
                binding.edtMinutes.updateText(it?.toString())
            }
        }

        seconds.observe(owner) {
            dialog?.apply {
                binding.edtSeconds.updateText(it?.toString())
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