package com.frolo.muse.ui.main.settings.sleeptimer

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import com.frolo.muse.R
import com.frolo.muse.ui.base.BaseDialogFragment
import com.frolo.muse.ui.main.settings.limitNumberInput
import kotlinx.android.synthetic.main.dialog_sleep_timer.*


/**
 * Simple implementation of Sleep Timer.
 */
class SleepTimerDialog : BaseDialogFragment() {

    private val listener: OnTimeSelectedListener?
        get() = (context as? OnTimeSelectedListener) ?: (parentFragment as? OnTimeSelectedListener)

    interface OnTimeSelectedListener {
        fun onTimeSelected(hours: Int, minutes: Int, seconds: Int)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_sleep_timer)
            setupDialogSize(this, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            loadUI(this)
        }
    }

    private fun loadUI(dialog: Dialog) = with(dialog) {
        edt_hours.limitNumberInput(23)

        edt_minutes.limitNumberInput(59)

        edt_seconds.limitNumberInput(59)

        btn_cancel.setOnClickListener {
            dismiss()
        }

        btn_save.setOnClickListener {
            val sHours = edt_hours.text.toString()
            val sMinutes = edt_minutes.text.toString()
            val sSeconds = edt_seconds.text.toString()
            val hours = if (sHours.isNotEmpty()) Integer.valueOf(sHours) else 0
            val minutes = if (sMinutes.isNotEmpty()) Integer.valueOf(sMinutes) else 0
            val seconds = if (sSeconds.isNotEmpty()) Integer.valueOf(sSeconds) else 0
            listener?.onTimeSelected(hours, minutes, seconds)
            dismiss()
        }
    }

    companion object {
        // Factory
        fun newInstance() = SleepTimerDialog()
    }

}
