package com.frolo.muse.admob

import com.frolo.muse.Logger
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

    override fun onAdFailedToLoad(i: Int) {
        log.invoke("Ad failed to load")
    }

    override fun onAdFailedToLoad(err: LoadAdError?) {
        log.invoke("Ad failed to load: $err")
    }

    override fun onAdLeftApplication() {
        log.invoke("Ad left application")
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

    companion object {
        fun createDefault(tag: String): LoggingAdListener {
            return LoggingAdListener { Logger.d(tag, it.orEmpty()) }
        }
    }
}