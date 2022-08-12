package com.frolo.muse.util

import android.content.Context
import android.graphics.Bitmap
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import androidx.annotation.StringRes


fun appendIcon(context: Context, text: CharSequence, icon: Bitmap, delimiter: String = " "): CharSequence {
    val imageSpan = ImageSpan(context, icon)

    val symbolToReplace = " "
    val builder = SpannableStringBuilder().append(text).append(delimiter + symbolToReplace)
    builder.setSpan(imageSpan, builder.length - 1, builder.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)

    return builder
}

fun appendIcon(context: Context, @StringRes stringId: Int, icon: Bitmap, delimiter: String = " "): CharSequence {
    return appendIcon(context, context.getString(stringId), icon, delimiter)
}

fun prependIcon(context: Context, text: CharSequence, icon: Bitmap, delimiter: String = " "): CharSequence {
    val imageSpan = ImageSpan(context, icon)

    val symbolToReplace = " "
    val builder = SpannableStringBuilder(symbolToReplace + delimiter).append(text)
    builder.setSpan(imageSpan, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)

    return builder
}

fun prependIcon(context: Context, @StringRes stringId: Int, icon: Bitmap, delimiter: String = " "): CharSequence {
    return prependIcon(context, context.getString(stringId), icon, delimiter)
}