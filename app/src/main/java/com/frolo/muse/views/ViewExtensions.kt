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

fun SeekBar.doOnProgressChanged(action: (seekBar: SeekBar, progress: Int, fromUser: Boolean) -> Unit) {
    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            action.invoke(seekBar, progress, fromUser)
        }
        override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
        override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
    })
}

fun Spinner.doOnItemSelected(action: (parent: AdapterView<*>, view: View?, position: Int, id: Long) -> Unit) {
    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>)= Unit
        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
            action.invoke(parent, view, position, id)
        }
    }
}