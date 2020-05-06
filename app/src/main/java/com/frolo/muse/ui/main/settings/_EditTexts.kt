package com.frolo.muse.ui.main.settings

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.annotation.ColorInt
import com.frolo.muse.R
import com.frolo.muse.StyleUtil
import kotlin.math.log10


/**
 * Calls [EditText.setText] under the hood, but only if the current [text] differs from the given one.
 * Used to avoid recursive call of [android.text.TextWatcher.afterTextChanged].
 */
fun EditText.updateText(text: CharSequence?) {
    val currentText = this.text?.toString()

    if (text.isNullOrEmpty() && currentText.isNullOrEmpty()) {
        return
    }

    if (text == currentText) {
        return
    }

    setText(text)
}

/**
 * Limits [this] EditText with the given [maxValue] so that
 * the user will not be able to input a number greater than [maxValue].
 */
fun EditText.limitNumberInput(maxValue: Int) {
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