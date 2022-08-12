package com.frolo.muse.rating

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import com.frolo.muse.R
import kotlinx.android.synthetic.main.dialog_rate.*


internal class RatingDialog constructor(
    context: Context,
    private val onButtonPressed: (
        dialog: RatingDialog,
        what: Button
    ) -> Unit
): Dialog(context) {

    enum class Button { POSITIVE, NEGATIVE, NEUTRAL }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_rate)
        setupDialogSize()
        setupDialogBackground()
        loadUi(this)
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

    private fun loadUi(dialog: Dialog) = with(dialog) {
        btn_positive.setOnClickListener {
            dispatchButtonPressed(Button.POSITIVE)
        }

        btn_negative.setOnClickListener {
            dispatchButtonPressed(Button.NEGATIVE)
        }

        btn_neutral.setOnClickListener {
            dispatchButtonPressed(Button.NEUTRAL)
        }
    }

    private fun dispatchButtonPressed(button: Button) {
        onButtonPressed.invoke(this, button)
    }
}
