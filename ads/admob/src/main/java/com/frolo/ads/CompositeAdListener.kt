package com.frolo.ads

import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError


class CompositeAdListener(private val listeners: List<AdListener>) : AdListener() {
    override fun onAdLoaded() {
        listeners.forEach { it.onAdLoaded() }
    }

    override fun onAdFailedToLoad(err: LoadAdError) {
        listeners.forEach { it.onAdFailedToLoad(err) }
    }

    override fun onAdImpression() {
        listeners.forEach { it.onAdImpression() }
    }

    override fun onAdClicked() {
        listeners.forEach { it.onAdClicked() }
    }

    override fun onAdOpened() {
        listeners.forEach { it.onAdOpened() }
    }

    override fun onAdClosed() {
        listeners.forEach { it.onAdClosed() }
    }
}