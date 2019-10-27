package com.frolo.muse.ui.main.audiofx.params

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleOwner
import com.frolo.muse.R
import com.frolo.muse.arch.observe
import com.frolo.muse.ui.base.BaseDialogFragment
import com.frolo.muse.views.observeProgress
import kotlinx.android.synthetic.main.dialog_playback_params.*


@RequiresApi(Build.VERSION_CODES.M)
class PlaybackParamsDialog : BaseDialogFragment() {

    companion object {

        // Factory
        fun newInstance() = PlaybackParamsDialog()
    }

    private val viewModel: PlaybackParamsViewModel by viewModel()

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

            initUI(this)
        }
    }

    private fun initUI(dialog: Dialog) {
        dialog.apply {
            cv_speed.apply {
                // speed range 0..2, controller range 0...200
                setMax(200)
                observeProgress { progress ->
                    val value = progress.toFloat() / 100
                    viewModel.onSeekSpeed(value)
                }
            }

            cv_pitch.apply {
                // pitch range 0..2, controller range 0..200
                setMax(200)
                observeProgress { progress ->
                    val value = progress.toFloat() / 100
                    viewModel.onSeekPitch(value)
                }
            }

            btn_cancel.setOnClickListener { dismiss() }
            btn_normalize.setOnClickListener { viewModel.onNormalizeButtonClicked() }
        }
    }

    private fun observeViewModel(owner: LifecycleOwner) {
        viewModel.apply {
            error.observe(owner) { err ->
                postError(err)
            }

            speed.observe(owner) { speed ->
                dialog?.apply {
                    cv_speed.progress = (speed * 100).toInt()
                }
            }

            pitch.observe(owner) { pitch ->
                dialog?.apply {
                    cv_pitch.progress = (pitch * 100).toInt()
                }
            }
        }
    }
}
