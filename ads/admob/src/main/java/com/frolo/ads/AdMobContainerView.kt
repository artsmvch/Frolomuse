package com.frolo.ads

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.FrameLayout
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

class AdMobContainerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {

    private var lastTimeAdViewAdded: Long = 0L

    private fun currentTimeMillis(): Long {
        return System.currentTimeMillis()
    }

    private fun findAdViews(): List<AdView> {
        val adViews = ArrayList<AdView>(1)
        for (childIndex in 0 until childCount) {
            val child = getChildAt(childIndex)
            if (child is AdView) {
                adViews.add(child)
            }
        }
        return adViews
    }

    fun loadBanner(unitId: String) {
        if (findAdViews().any { it.adUnitId == unitId }) {
            return
        }

        val adView = AdView(context)

        removeAllViews()
        addView(adView)

        adView.alpha = 0f
        val adListener = AdListenerBuilder()
            .doWhenAdLoaded {
                lastTimeAdViewAdded = currentTimeMillis()
                adView.alpha = 0f
                adView.animate()
                    .alpha(1f)
                    .setDuration(ANIMATION_APPEARANCE_DURATION)
                    .start()
            }
            .doLogging { msg ->
                if (BuildConfig.DEBUG) {
                    Log.d(LOG_TAG, msg.orEmpty())
                }
            }
            .build()
        val adRequest = AdRequest.Builder()
            .build()
        adView.adListener = adListener
        adView.setAdSize(AdMobUtils.calculateSmartBannerAdSize(context))
        adView.adUnitId = unitId
        adView.loadAd(adRequest)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (currentTimeMillis() - lastTimeAdViewAdded < CLICK_THROTTLING_DURATION) {
            return true
        }
        return super.onInterceptTouchEvent(ev)
    }

    fun clearBanner() {
        findAdViews().forEach { it.destroy() }
        removeAllViews()
    }

    companion object {
        private const val LOG_TAG = "AdMobContainerView"

        private const val CLICK_THROTTLING_DURATION = 300L

        private const val ANIMATION_APPEARANCE_DURATION = 120L
    }
}