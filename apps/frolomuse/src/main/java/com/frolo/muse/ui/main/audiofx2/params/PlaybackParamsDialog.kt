package com.frolo.muse.ui.main.audiofx2.params

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import android.widget.CompoundButton
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import com.frolo.arch.support.observeNonNull
import com.frolo.muse.databinding.DialogPlaybackParamsBinding
import com.frolo.muse.ui.base.BaseDialogFragment
import com.frolo.muse.views.doOnProgressChanged


class PlaybackParamsDialog : BaseDialogFragment() {
    private var _binding: DialogPlaybackParamsBinding? = null
    private val binding: DialogPlaybackParamsBinding get() = _binding!!

    private val viewModel: PlaybackParamsViewModel by viewModel()

    private val doNotPersistCheckedChangeListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        viewModel.onDoNotPersistPlaybackParamsToggled(isChecked)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeViewModel(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            _binding = DialogPlaybackParamsBinding.inflate(layoutInflater)

            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(binding.root)

            val width = resources.displayMetrics.widthPixels
            setupDialogSize(this, (width * 19) / 20, ViewGroup.LayoutParams.WRAP_CONTENT)

            loadUi(this)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadUi(dialog: Dialog) = with(dialog) {
        binding.sbSpeed.apply {
            // speed range 0..2, controller range 0...200
            max = 200
            doOnProgressChanged { _, progress, _ ->
                val value = progress.toFloat() / 100
                viewModel.onSeekSpeed(value)
            }
        }

        binding.sbPitch.apply {
            // pitch range 0..2, controller range 0..200
            max = 200
            doOnProgressChanged { _, progress, _ ->
                val value = progress.toFloat() / 100
                viewModel.onSeekPitch(value)
            }
        }

        binding.chbDoNotPersist.setOnCheckedChangeListener(doNotPersistCheckedChangeListener)

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnNormalize.setOnClickListener {
            viewModel.onNormalizeButtonClicked()
        }
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        error.observeNonNull(owner) { err ->
            postError(err)
        }

        isPersistenceAvailable.observeNonNull(owner) { isAvailable ->
            dialog?.apply {
                binding.chbDoNotPersist.isVisible = isAvailable
            }
        }

        doNotPersistPlaybackParams.observeNonNull(owner) { doNotPersist ->
            dialog?.apply { binding.apply {
                chbDoNotPersist.setOnCheckedChangeListener(null)
                chbDoNotPersist.isChecked = doNotPersist
                chbDoNotPersist.setOnCheckedChangeListener(doNotPersistCheckedChangeListener)
            } }
        }

        speed.observeNonNull(owner) { speed ->
            dialog?.apply {
                binding.sbSpeed.progress = (speed * 100).toInt()
            }
        }

        pitch.observeNonNull(owner) { pitch ->
            dialog?.apply {
                binding.sbPitch.progress = (pitch * 100).toInt()
            }
        }
    }

    companion object {

        // Factory
        fun newInstance() = PlaybackParamsDialog()

    }

}
