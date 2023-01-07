package com.frolo.muse.ui.main.library

import android.app.Application
import androidx.appcompat.view.ContextThemeWrapper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.ads.AdListenerBuilder
import com.frolo.ads.AdSizeCompat
import com.frolo.arch.support.distinctUntilChanged
import com.frolo.logger.api.Logger
import com.frolo.muse.BuildInfo
import com.frolo.muse.R
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.ui.base.BaseAndroidViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import javax.inject.Inject


class LibraryViewModel @Inject constructor(
    application: Application,
    eventLogger: EventLogger
): BaseAndroidViewModel(application, eventLogger) {

    private val _adView by lazy {
        MutableLiveData<AdView>(null).apply {
            loadAdViewAsync(this)
        }
    }
    val adView: LiveData<AdView> get() = _adView.distinctUntilChanged()

    private fun loadAdViewAsync(target: MutableLiveData<AdView>) {
        val context = ContextThemeWrapper(justApplication,
            com.google.android.material.R.style.Theme_Material3_Dark)
        val adView = AdView(context)
        val adUnitId = if (BuildInfo.isDebug()) {
            R.string.admob_ad_unit_id_test
        } else {
            R.string.admob_ad_unit_id_main_screen
        }
        val adListener = AdListenerBuilder()
            .doWhenAdLoaded { target.value = adView }
            .doWhenAdFailedToLoad { target.value = null }
            .doLogging { msg -> Logger.d(LOG_TAG, msg) }
            .build()
        val adRequest = AdRequest.Builder()
            .build()
        adView.adListener = adListener
        adView.setAdSize(AdSizeCompat.calculateSmartBannerAdSize(context))
        adView.adUnitId = context.getString(adUnitId)
        adView.loadAd(adRequest)
    }

    companion object {
        private const val LOG_TAG = "LibraryViewModel"
    }

}