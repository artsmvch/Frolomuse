package com.frolo.muse.ui.main.settings.sleeptimer

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.frolo.muse.R
import com.frolo.muse.StyleUtil
import com.frolo.muse.ui.base.BaseDialogFragment
import kotlinx.android.synthetic.main.dialog_sleep_timer.*
import kotlin.math.log10


// Simple hours-minutes-seconds picker
class SleepTimerDialog : BaseDialogFragment() {

    companion object {
        // Factory
        fun newInstance() = SleepTimerDialog()
    }

    private var listener: OnTimeSelectedListener? = null

    interface OnTimeSelectedListener {
        // pass null is nothing selected in both hour or minute sections
        fun onTimeSelected(hours: Int, minutes: Int, seconds: Int)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = (context as? OnTimeSelectedListener) ?: (parentFragment as? OnTimeSelectedListener)
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_sleep_timer)
            setupDialogSize(this, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            initUI(this)
        }
    }

    private fun initUI(dialog: Dialog) {
        dialog.apply {
            edt_hours.addTextChangedListener(wrapMaxIntValidator(edt_hours, 23))
            edt_minutes.addTextChangedListener(wrapMaxIntValidator(edt_minutes, 59))
            edt_seconds.addTextChangedListener(wrapMaxIntValidator(edt_seconds, 59))
            btn_cancel.setOnClickListener { dismiss() }
            btn_done.setOnClickListener {
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
    }

    private fun wrapMaxIntValidator(editor: EditText, maxValue: Int): TextWatcher {
        if (maxValue < 0)
            throw IllegalArgumentException("Max value shouldn't be < 0")

        val length = (log10(maxValue.toDouble()) + 1).toInt()

        return object : TextWatcher {
            private var update = false
            private val animator: ValueAnimator
            private val animUpdateListener = ValueAnimator.AnimatorUpdateListener { anim -> editor.setTextColor(anim.animatedValue as Int) }

            init {
                @ColorInt val red = ContextCompat.getColor(editor.context, R.color.scarlet)
                @ColorInt val textColor = StyleUtil.readColorAttrValue(editor.context, android.R.attr.textColor)
                animator = ValueAnimator.ofInt(red, textColor).apply {
                    setEvaluator(ArgbEvaluator())
                    duration = 300
                    addUpdateListener(animUpdateListener)
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                update = false
                if (s.isEmpty()) {
                    update = false
                } else if (s.length > length) {
                    update = true
                } else { // between 1 and {length} chars
                    for (i in 0 until s.length) {
                        val ch = s[i]
                        if (!Character.isDigit(ch)) {
                            update = true
                            break
                        }
                    }
                    val numberValue = Integer.valueOf(s.toString())
                    if (numberValue > maxValue)
                        update = true
                }
            }

            override fun afterTextChanged(s: Editable) {
                if (update) {
                    val sb = StringBuilder()
                    var currLength = 0
                    for (i in 0 until s.length) {
                        val ch = s[i]
                        if (Character.isDigit(ch)) {
                            sb.append(ch)
                            currLength++
                            if (currLength >= length) break
                        }
                    }

                    var newValue = Integer.valueOf(sb.toString())
                    if (newValue > maxValue)
                        newValue = maxValue

                    val sValue = newValue.toString()
                    val selection = sValue.length
                    editor.setText(sValue)
                    editor.setSelection(selection)
                    animator.start()
                }
            }
        }
    }
}
