package com.frolo.muse.views

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Spinner
import androidx.appcompat.app.ActionBar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView

fun View.setBackgroundColorCompat(color: Int) {
    if (this is androidx.cardview.widget.CardView) {
        setCardBackgroundColor(color)
    } else {
        setBackgroundColor(color)
    }
}

fun ActionBar.showBackArrow() {
    setDisplayShowHomeEnabled(true)
    setDisplayHomeAsUpEnabled(true)
}

fun EditText.getNonNullText(): String {
    return text?.toString() ?: ""
}

fun EditText.observerInput(observer: (text: String) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            observer(s?.toString() ?: "")
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
    })
}

fun SeekBar.observeProgress(observer: (progress: Int) -> Unit) {
    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            observer(progress)
        }
        override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
        override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
    })
}

fun ControllerView.observeProgress(observer: (progress: Int) -> Unit) {
    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            observer(progress)
        }
        override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
        override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
    })
}

fun Spinner.observeSelection(observer: (adapterView: AdapterView<*>, position: Int) -> Unit) {
    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(adapterView: AdapterView<*>)= Unit
        override fun onItemSelected(adapterView: AdapterView<*>, view: View?, position: Int, id: Long) {
            observer(adapterView, position)
        }
    }
}

fun RecyclerView.clearDecorations() {
    for (i in 0 until itemDecorationCount) {
        removeItemDecorationAt(i)
    }
}

@Suppress("UNCHECKED_CAST")
fun <T : CoordinatorLayout.Behavior<*>> View.findBehavior(): T = layoutParams.let { params ->
    if (params !is CoordinatorLayout.LayoutParams)
        throw IllegalArgumentException("View's layout params should be CoordinatorLayout.LayoutParams")

    params.behavior as? T ?: throw IllegalArgumentException("Layout's behavior is not current behavior")
}