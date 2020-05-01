package com.frolo.muse.ui.main.settings.sleeptimer

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import androidx.annotation.ColorInt
import com.frolo.muse.R
import com.frolo.muse.StyleUtil
import com.frolo.muse.ui.base.BaseDialogFragment
import kotlinx.android.synthetic.main.dialog_sleep_timer.*
import kotlin.math.log10


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

/**
 * Limits [this] EditText with the given [maxValue] so that
 * the user will not be able to input a number greater than [maxValue].
 */
private fun EditText.limitNumberInput(maxValue: Int) {
    if (maxValue < 0) {
        throw IllegalArgumentException("Max value cannot be lower than 0")
    }

    val length = (log10(maxValue.toDouble()) + 1).toInt()

    val editText = this

    val textWatcher =  object : TextWatcher {
        private var needUpdate = false
        private val animator: ValueAnimator
        private val animUpdateListener =
                ValueAnimator.AnimatorUpdateListener { anim ->
                    editText.setTextColor(anim.animatedValue as Int)
                }

        init {
            @ColorInt val red = StyleUtil.readColorAttrValue(editText.context, R.attr.errorTextColor)
            @ColorInt val textColor = StyleUtil.readColorAttrValue(editText.context, R.attr.colorOnSurface)
            animator = ValueAnimator.ofInt(red, textColor).apply {
                setEvaluator(ArgbEvaluator())
                duration = 300
                addUpdateListener(animUpdateListener)
            }
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            needUpdate = false
            if (s.isEmpty()) {
                needUpdate = false
            } else if (s.length > length) {
                needUpdate = true
            } else { // between 1 and {length} chars
                for (element in s) {
                    if (!Character.isDigit(element)) {
                        needUpdate = true
                        break
                    }
                }
                val numberValue = Integer.valueOf(s.toString())
                if (numberValue > maxValue) {
                    needUpdate = true
                }
            }
        }

        override fun afterTextChanged(s: Editable) {
            if (needUpdate) {
                val sb = StringBuilder()
                var currLength = 0
                for (element in s) {
                    if (Character.isDigit(element)) {
                        sb.append(element)
                        currLength++
                        if (currLength >= length) break
                    }
                }

                var newValue = Integer.valueOf(sb.toString())
                if (newValue > maxValue)
                    newValue = maxValue

                val sValue = newValue.toString()
                val selection = sValue.length
                editText.setText(sValue)
                editText.setSelection(selection)
                animator.start()
            }
        }
    }

    editText.addTextChangedListener(textWatcher)
}
