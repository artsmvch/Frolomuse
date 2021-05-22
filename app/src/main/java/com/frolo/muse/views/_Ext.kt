package com.frolo.muse.views

import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Spinner
import androidx.appcompat.app.ActionBar


fun ActionBar.showBackArrow() {
    setDisplayShowHomeEnabled(true)
    setDisplayHomeAsUpEnabled(true)
}

fun EditText.getNonNullText(): String {
    return text?.toString() ?: ""
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

fun Spinner.observeSelection(observer: (adapterView: AdapterView<*>, position: Int) -> Unit) {
    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(adapterView: AdapterView<*>)= Unit
        override fun onItemSelected(adapterView: AdapterView<*>, view: View?, position: Int, id: Long) {
            observer(adapterView, position)
        }
    }
}