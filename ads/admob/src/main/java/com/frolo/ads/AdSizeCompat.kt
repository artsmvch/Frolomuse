package com.frolo.ads

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import com.google.android.gms.ads.AdSize

object AdSizeCompat {
    fun calculateSmartBannerAdSize(context: Context): AdSize {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            ?: return AdSize.SMART_BANNER
        val display = windowManager.defaultDisplay
            ?: return AdSize.SMART_BANNER
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)
        val adWidth = (outMetrics.widthPixels / outMetrics.density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
    }
}