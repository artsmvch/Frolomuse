package com.frolo.muse.ui.main.audiofx.params

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import android.widget.CompoundButton
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import com.frolo.muse.R
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.ui.base.BaseDialogFragment
import com.frolo.muse.views.doOnProgressChanged
import kotlinx.android.synthetic.main.dialog_playback_params.*


@RequiresApi(Build.VERSION_CODES.M)
class PlaybackParamsDialog : BaseDialogFragment() {

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
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_playback_params)

            val width = resources.displayMetrics.widthPixels
            setupDialogSize(this, (width * 19) / 20, ViewGroup.LayoutParams.WRAP_CONTENT)

            loadUI(this)
        }
    }

    private fun loadUI(dialog: Dialog) = with(dialog) {
        sb_speed.apply {
            // speed range 0..2, controller range 0...200
            max = 200
            doOnProgressChanged { _, progress, _ ->
                val value = progress.toFloat() / 100
                viewModel.onSeekSpeed(value)
            }
        }

        sb_pitch.apply {
            // pitch range 0..2, controller range 0..200
            max = 200
            doOnProgressChanged { _, progress, _ ->
                val value = progress.toFloat() / 100
                viewModel.onSeekPitch(value)
            }
        }

        chb_do_not_persist.setOnCheckedChangeListener(doNotPersistCheckedChangeListener)

        btn_cancel.setOnClickListener {
            dismiss()
        }

        btn_normalize.setOnClickListener {
            viewModel.onNormalizeButtonClicked()
        }
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        error.observeNonNull(owner) { err ->
            postError(err)
        }

        isPersistenceAvailable.observeNonNull(owner) { isAvailable ->
            dialog?.apply {
                chb_do_not_persist.isVisible = isAvailable
            }
        }

        doNotPersistPlaybackParams.observeNonNull(owner) { doNotPersist ->
            dialog?.apply {
                chb_do_not_persist.setOnCheckedChangeListener(null)
                chb_do_not_persist.isChecked = doNotPersist
                chb_do_not_persist.setOnCheckedChangeListener(doNotPersistCheckedChangeListener)
            }
        }

        speed.observeNonNull(owner) { speed ->
            dialog?.apply {
                sb_speed.progress = (speed * 100).toInt()
            }
        }

        pitch.observeNonNull(owner) { pitch ->
            dialog?.apply {
                sb_pitch.progress = (pitch * 100).toInt()
            }
        }
    }

    companion object {

        // Factory
        fun newInstance() = PlaybackParamsDialog()

    }

}
