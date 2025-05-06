package com.frolo.muse.ui.main.settings.sleeptimer

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import com.frolo.muse.R
import com.frolo.muse.ui.base.BaseDialogFragment
import com.frolo.muse.ui.main.settings.limitNumberInput


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
            loadUi(this)
        }
    }

    private fun loadUi(dialog: Dialog) = with(dialog) {
        val edtHours = findViewById<EditText>(R.id.edt_hours)
        val edtMinutes = findViewById<EditText>(R.id.edt_minutes)
        val edtSeconds = findViewById<EditText>(R.id.edt_seconds)
        val btnSave = findViewById<View>(R.id.btn_save)
        val btnCancel = findViewById<View>(R.id.btn_cancel)

        edtHours.limitNumberInput(23)
        edtMinutes.limitNumberInput(59)
        edtSeconds.limitNumberInput(59)

        btnSave.setOnClickListener {
            val sHours = edtHours.text.toString()
            val sMinutes = edtMinutes.text.toString()
            val sSeconds = edtSeconds.text.toString()
            val hours = if (sHours.isNotEmpty()) Integer.valueOf(sHours) else 0
            val minutes = if (sMinutes.isNotEmpty()) Integer.valueOf(sMinutes) else 0
            val seconds = if (sSeconds.isNotEmpty()) Integer.valueOf(sSeconds) else 0
            listener?.onTimeSelected(hours, minutes, seconds)
            dismiss()
        }

        btnCancel.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        // Factory
        fun newInstance() = SleepTimerDialog()
    }

}
