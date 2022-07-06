package com.frolo.core.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.frolo.core.ui.R


internal class AlertDialogBuilderImpl(
    private var context: Context
) : AlertDialogBuilder {

    private var icon: Drawable? = null
    private var title: CharSequence? = null
    private var message: CharSequence? = null
    private var positiveButton: ButtonInfo? = null
    private var negativeButton: ButtonInfo? = null
    private var neutralButton: ButtonInfo? = null

    override fun build(): Dialog {
        val dialog = Dialog(context).apply {
            window?.setBackgroundDrawable(
                ContextCompat.getDrawable(context, R.drawable.simple_alert_dialog))
            window?.setWindowAnimations(R.style.AlertDialog_Simple)

            setContentView(R.layout.simple_alert_dialog)
            findViewById<TextView>(R.id.title).text = title
            findViewById<TextView>(R.id.message).text = message
            findViewById<Button>(R.id.button_positive).also { button ->
                bindButton(this, button, positiveButton)
            }
            findViewById<Button>(R.id.button_negative).also { button ->
                bindButton(this, button, negativeButton)
            }
            findViewById<Button>(R.id.button_neutral).also { button ->
                bindButton(this, button, neutralButton)
            }
        }
        return dialog
    }

    private fun bindButton(dialog: Dialog, button: Button, info: ButtonInfo?) {
        if (info != null) {
            button.visibility = View.VISIBLE
            button.text = info.text
            button.setOnClickListener {
                dialog.dismiss()
                info.onClickListener.invoke()
            }
        } else {
            button.visibility = View.GONE
        }
    }

    override fun show(): Dialog {
        return build().apply { show() }
    }

    override fun setIcon(drawable: Drawable?): AlertDialogBuilder {
        this.icon = drawable
        return this
    }

    override fun setIcon(@DrawableRes drawableId: Int): AlertDialogBuilder {
        icon = AppCompatResources.getDrawable(context, drawableId)
        return this
    }

    override fun setTitle(@StringRes resId: Int): AlertDialogBuilder {
        title = context.getString(resId)
        return this
    }

    override fun setTitle(title: CharSequence): AlertDialogBuilder {
        this.title = title
        return this
    }

    override fun setMessage(@StringRes resId: Int): AlertDialogBuilder {
        message = context.getString(resId)
        return this
    }

    override fun setMessage(message: CharSequence): AlertDialogBuilder {
        this.message = message
        return this
    }

    override fun setPositiveButton(
        @StringRes resId: Int,
        onClickListener: () -> Unit
    ): AlertDialogBuilder {
        positiveButton = ButtonInfo(
            text = context.getString(resId),
            onClickListener = onClickListener
        )
        return this
    }

    override fun setNegativeButton(
        @StringRes resId: Int,
        onClickListener: () -> Unit
    ): AlertDialogBuilder {
        negativeButton = ButtonInfo(
            text = context.getString(resId),
            onClickListener = onClickListener
        )
        return this
    }

    override fun setNeutralButton(
        @StringRes resId: Int,
        onClickListener: () -> Unit
    ): AlertDialogBuilder {
        neutralButton = ButtonInfo(
            text = context.getString(resId),
            onClickListener = onClickListener
        )
        return this
    }

    private class ButtonInfo(
        val text: String,
        val onClickListener: () -> Unit
    )
}