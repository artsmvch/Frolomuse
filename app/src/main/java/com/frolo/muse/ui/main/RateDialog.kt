package com.frolo.muse.ui.main

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import com.frolo.muse.R
import kotlinx.android.synthetic.main.dialog_rate.*


class RateDialog constructor(
    context: Context,
    private val onButtonPressed: (dialog: RateDialog, what: Button) -> Unit
): Dialog(context) {

    enum class Button { NO, REMIND_LATER, RATE }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_rate)
        setupDialogSize()
        setupDialogBackground()
        loadUI(this)
    }

    private fun setupDialogSize() {
        window?.also { window ->
            val metrics = context.resources.displayMetrics
            val width = metrics.widthPixels
            val height = metrics.heightPixels

            window.setLayout(11 * width / 12, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    private fun setupDialogBackground() {
        window?.let { window ->
            ll_content.background = window.decorView.background
            window.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    private fun loadUI(dialog: Dialog) = with(dialog) {
        btn_no.setOnClickListener {
            dispatchButtonPressed(Button.NO)
        }

        btn_rate.setOnClickListener {
            dispatchButtonPressed(Button.RATE)
        }

        btn_remind_later.setOnClickListener {
            dispatchButtonPressed(Button.REMIND_LATER)
        }
    }

    private fun dispatchButtonPressed(button: Button) {
        onButtonPressed.invoke(this, button)
    }
}
