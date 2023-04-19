package com.frolo.muse.ui.main.library

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import com.facebook.ads.*
import com.frolo.logger.api.Logger

class FacebookAdViewContainer @JvmOverloads constructor(
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

    fun loadBanner(placementId: String) {
        if (findAdViews().any { it.placementId == placementId }) {
            return
        }

        val adView = AdView(context, placementId, AdSize.BANNER_HEIGHT_50)

        removeAllViews()
        addView(adView)

        val adListener = object : AdListener {
            override fun onError(ad: Ad?, err: AdError?) {
                Logger.e(LOG_TAG, "onError:$err")
            }
            override fun onAdLoaded(ad: Ad?) {
                Logger.e(LOG_TAG, "adLoaded")
                lastTimeAdViewAdded = currentTimeMillis()
            }
            override fun onAdClicked(ad: Ad?) {
                Logger.e(LOG_TAG, "adClicked")
            }
            override fun onLoggingImpression(ad: Ad?) {
                Logger.e(LOG_TAG, "onLoggingImpression")
            }
        }
        val config = adView.buildLoadAdConfig()
            .withAdListener(adListener)
            .build()
        adView.loadAd(config)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (currentTimeMillis() - lastTimeAdViewAdded < CLICK_THROTTLING_DURATION) {
            // return true
        }
        return super.onInterceptTouchEvent(ev)
    }

    fun clearBanner() {
        findAdViews().forEach { it.destroy() }
        removeAllViews()
    }

    companion object {
        private const val LOG_TAG = "FacebookAdViewContainer"

        private const val CLICK_THROTTLING_DURATION = 300L

        private const val ANIMATION_APPEARANCE_DURATION = 120L
    }
}