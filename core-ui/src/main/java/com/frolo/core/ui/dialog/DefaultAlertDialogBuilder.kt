package com.frolo.core.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.Drawable
import com.google.android.material.dialog.MaterialAlertDialogBuilder

internal class DefaultAlertDialogBuilder(
    private val context: Context
) : AlertDialogBuilder {

    private val builder: MaterialAlertDialogBuilder by lazy {
        MaterialAlertDialogBuilder(context)
    }

    override fun build(): Dialog {
        return builder.create()
    }

    override fun show(): Dialog {
        return builder.show()
    }

    override fun setIcon(drawableId: Int): AlertDialogBuilder {
        builder.setIcon(drawableId)
        return this
    }

    override fun setIcon(drawable: Drawable?): AlertDialogBuilder {
        builder.setIcon(drawable)
        return this
    }

    override fun setTitle(resId: Int): AlertDialogBuilder {
        builder.setTitle(resId)
        return this
    }

    override fun setTitle(title: CharSequence): AlertDialogBuilder {
        builder.setTitle(title)
        return this
    }

    override fun setMessage(resId: Int): AlertDialogBuilder {
        builder.setMessage(resId)
        return this
    }

    override fun setMessage(message: CharSequence): AlertDialogBuilder {
        builder.setMessage(message)
        return this
    }

    override fun setPositiveButton(resId: Int, onClickListener: () -> Unit): AlertDialogBuilder {
        builder.setPositiveButton(resId) { _, _ -> onClickListener.invoke() }
        return this
    }

    override fun setNegativeButton(resId: Int, onClickListener: () -> Unit): AlertDialogBuilder {
        builder.setNegativeButton(resId) { _, _ -> onClickListener.invoke() }
        return this
    }

    override fun setNeutralButton(resId: Int, onClickListener: () -> Unit): AlertDialogBuilder {
        builder.setNeutralButton(resId) { _, _ -> onClickListener.invoke() }
        return this
    }
}