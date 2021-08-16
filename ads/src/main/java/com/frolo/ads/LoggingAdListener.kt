package com.frolo.ads

import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError


/**
 * An extension for [AdListener] that logs a message for each ad callback.
 */
class LoggingAdListener constructor(
    private val log: (msg: String?) -> Unit
): AdListener() {

    override fun onAdClosed() {
        log.invoke("Ad closed")
    }

    override fun onAdFailedToLoad(err: LoadAdError?) {
        log.invoke("Ad failed to load: $err")
    }

    override fun onAdOpened() {
        log.invoke("Ad opened")
    }

    override fun onAdLoaded() {
        log.invoke("Ad loaded")
    }

    override fun onAdClicked() {
        log.invoke("Ad clicked")
    }

    override fun onAdImpression() {
        log.invoke("Ad impression")
    }
}