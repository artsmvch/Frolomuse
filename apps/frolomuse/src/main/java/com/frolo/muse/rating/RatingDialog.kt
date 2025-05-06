package com.frolo.muse.rating

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.frolo.muse.R


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

    private fun loadUi(dialog: Dialog) = with(dialog) {
        window?.let { window ->
            findViewById<View>(R.id.ll_content).background = window.decorView.background
            window.setBackgroundDrawableResource(android.R.color.transparent)
        }

        findViewById<View>(R.id.btn_positive).setOnClickListener {
            dispatchButtonPressed(Button.POSITIVE)
        }

        findViewById<View>(R.id.btn_negative).setOnClickListener {
            dispatchButtonPressed(Button.NEGATIVE)
        }

        findViewById<View>(R.id.btn_neutral).setOnClickListener {
            dispatchButtonPressed(Button.NEUTRAL)
        }
    }

    private fun dispatchButtonPressed(button: Button) {
        onButtonPressed.invoke(this, button)
    }
}
