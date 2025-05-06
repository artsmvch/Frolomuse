package com.frolo.audiofx2.app.ui.instruction

import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.frolo.audiofx.app.R
import com.frolo.audiofx2.app.attachinfo.AudioFx2AttachInfoHelper
import com.google.android.material.bottomsheet.BottomSheetDialog


internal class InstructionDialog constructor(
    context: Context
): BottomSheetDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_instruction)
        loadUi()
    }

    private fun loadUi() = with(this) {
        val attachInfo = AudioFx2AttachInfoHelper.default(context)
        findViewById<ImageView>(R.id.icon)?.setImageDrawable(attachInfo.icon)
        findViewById<TextView>(R.id.title)?.text = attachInfo.name
        findViewById<TextView>(R.id.description)?.text = attachInfo.description
    }

}