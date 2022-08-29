package com.frolo.audiofx2.app.ui.instruction

import android.content.Context
import android.os.Bundle
import com.frolo.audiofx.app.R
import com.frolo.audiofx2.app.attachinfo.AudioFx2AttachInfoHelper
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.dialog_instruction.*


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
        icon.setImageDrawable(attachInfo.icon)
        title.text = attachInfo.name
        description.text = attachInfo.description
    }

}