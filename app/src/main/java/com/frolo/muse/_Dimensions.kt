package com.frolo.muse

import android.content.Context


@Deprecated("Use Screen.dpFloat", ReplaceWith("Screen.dpFloat(context, this)"))
fun Float.dp2px(context: Context): Float {
    return Screen.dpFloat(context, this)
}

@Deprecated("Use Screen.dpFloat", ReplaceWith("Screen.dpFloat(this)"))
fun Float.dp2px(): Float {
    return Screen.dpFloat(this)
}

@Deprecated("Use Screen.spFloat", ReplaceWith("Screen.spFloat(context, this)"))
fun Float.sp2px(context: Context): Float {
    return Screen.spFloat(context, this)
}

@Deprecated("Use Screen.getScreenWidth", ReplaceWith("Screen.getScreenWidth(this)"))
val Context.screenWidth: Int
    get() {
        return Screen.getScreenWidth(this)
    }