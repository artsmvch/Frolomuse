package com.frolo.core.ui.dialog

import android.app.Dialog
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes


internal val EMPTY_ON_CLICK_LISTENER: () -> Unit = { }

interface AlertDialogBuilder {
    fun build(): Dialog
    fun show(): Dialog

    fun setIcon(@DrawableRes drawableId: Int): AlertDialogBuilder
    fun setIcon(drawable: Drawable?): AlertDialogBuilder
    fun setTitle(@StringRes resId: Int): AlertDialogBuilder
    fun setTitle(title: CharSequence): AlertDialogBuilder
    fun setMessage(@StringRes resId: Int): AlertDialogBuilder
    fun setMessage(message: CharSequence): AlertDialogBuilder
    fun setPositiveButton(
        @StringRes resId: Int,
        onClickListener: (() -> Unit) = EMPTY_ON_CLICK_LISTENER
    ): AlertDialogBuilder
    fun setNegativeButton(
        @StringRes resId: Int,
        onClickListener: (() -> Unit) = EMPTY_ON_CLICK_LISTENER
    ): AlertDialogBuilder
    fun setNeutralButton(
        @StringRes resId: Int,
        onClickListener: (() -> Unit) = EMPTY_ON_CLICK_LISTENER
    ): AlertDialogBuilder
}