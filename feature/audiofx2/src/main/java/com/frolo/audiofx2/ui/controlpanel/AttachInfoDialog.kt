package com.frolo.audiofx2.ui.controlpanel

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialog
import com.frolo.audiofx2.ui.AudioFx2AttachInfo
import com.frolo.audiofx2.ui.databinding.DialogAttachInfoBinding
import com.frolo.ui.Screen

internal class AttachInfoDialog(
    context: Context,
    private val attachInfo: AudioFx2AttachInfo
): AppCompatDialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DialogAttachInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpWindow()
        binding.apply {
            icon.setImageDrawable(attachInfo.icon)
            title.text = attachInfo.name
            description.text = attachInfo.description
            close.setOnClickListener { dismiss() }
        }
    }

    private fun setUpWindow() {
        val window = this.window ?: return
        val dialogWidth: Int = (Screen.getScreenWidth(context) * 0.95).toInt()
        val dialogHeight: Int = ViewGroup.LayoutParams.WRAP_CONTENT
        window.setLayout(dialogWidth, dialogHeight)
    }
}