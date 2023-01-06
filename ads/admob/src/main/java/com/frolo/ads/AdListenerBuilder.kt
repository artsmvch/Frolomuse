package com.frolo.ads

import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError


/**
 * A builder that combines several actions for different ad events into one [AdListener].
 */
class AdListenerBuilder {
    private val listeners = ArrayList<AdListener>()

    fun build(): AdListener = CompositeAdListener(listeners)

    fun buildAndSetIn(adView: AdView) {
        adView.adListener = build()
    }

    fun addListener(listener: AdListener): AdListenerBuilder {
        listeners.add(listener)
        return this
    }

    fun doLogging(log: (msg: String?) -> Unit): AdListenerBuilder {
        val l = LoggingAdListener(log)
        listeners.add(l)
        return this
    }

    fun doWhenAdLoaded(action: () -> Unit): AdListenerBuilder {
        val l = object : AdListener() {
            override fun onAdLoaded() {
                action.invoke()
            }
        }
        listeners.add(l)
        return this
    }

    fun doWhenAdFailedToLoad(action: (LoadAdError?) -> Unit): AdListenerBuilder {
        val l = object : AdListener() {
            override fun onAdFailedToLoad(err: LoadAdError) {
                action.invoke(err)
            }
        }
        listeners.add(l)
        return this
    }
}