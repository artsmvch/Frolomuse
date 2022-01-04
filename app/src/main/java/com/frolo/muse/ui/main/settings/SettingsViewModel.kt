package com.frolo.muse.ui.main.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.BuildConfig
import com.frolo.muse.arch.*
import com.frolo.muse.billing.Products
import com.frolo.muse.engine.Player
import com.frolo.muse.interactor.billing.PremiumManager
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.ProductOfferUiElementSource
import com.frolo.muse.logger.logProductOffered
import com.frolo.muse.router.AppRouter
import com.frolo.muse.repository.AppearancePreferences
import com.frolo.muse.repository.Preferences
import com.frolo.muse.repository.RemoteConfigRepository
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.PremiumViewModel
import io.reactivex.Flowable
import javax.inject.Inject


class SettingsViewModel @Inject constructor(
    private val player: Player,
    private val schedulerProvider: SchedulerProvider,
    private val appRouter: AppRouter,
    private val premiumManager: PremiumManager,
    private val remoteConfigRepository: RemoteConfigRepository,
    private val preferences: Preferences,
    private val appearancePreferences: AppearancePreferences,
    private val eventLogger: EventLogger
): PremiumViewModel(schedulerProvider, appRouter, premiumManager, eventLogger) {

    val isBuyPremiumOptionVisible: LiveData<Boolean> by lazy {
        liveDataOf(false)
    }

    val isDonateOptionVisible: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().apply {
            remoteConfigRepository.isDonationFeatureEnabled()
                .observeOn(schedulerProvider.main())
                .subscribeFor { isEnabled -> value = isEnabled }
        }
    }

    private val _notifyPremiumProductConsumedEvent = SingleLiveEvent<Unit>()
    val notifyPremiumProductConsumedEvent: LiveData<Unit> get() = _notifyPremiumProductConsumedEvent

    private val _notifyPremiumTrialResetEvent = SingleLiveEvent<Unit>()
    val notifyPremiumTrialResetEvent: LiveData<Unit> get() = _notifyPremiumTrialResetEvent

    val snowfallOptionVisible: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false).apply {
            val source1 = remoteConfigRepository.isSnowfallFeatureEnabled().toFlowable()
            val source2 = appearancePreferences.isSnowfallEnabled()
            Flowable.combineLatest(source1, source2) { isFeatureEnabled, isLocallyEnabled ->
                    // Only if the feature is enabled remotely
                    isFeatureEnabled// || isLocallyEnabled
                }
                .observeOn(schedulerProvider.main())
                .subscribeFor { value = it }
        }
    }

    fun onBuyPremiumPreferenceClicked() {
        eventLogger.logProductOffered(Products.PREMIUM, ProductOfferUiElementSource.SETTINGS)
        appRouter.offerToBuyPremium(allowTrialActivation = true)
    }

    fun onPlaybackFadingClick() {
        appRouter.openPlaybackFadingParams()
    }

    /**
     * [!] For debugging only.
     */
    fun onConsumePremiumProductClicked() {
        assertNotDebug("Consume premium product")
        premiumManager.consumeProduct(Products.PREMIUM)
            .observeOn(schedulerProvider.main())
            .subscribeFor {
                _notifyPremiumProductConsumedEvent.call()
            }
    }

    /**
     * [!] For debugging only.
     */
    fun onResetPremiumTrialClicked() {
        assertNotDebug("Reset premium trial")
        premiumManager.resetTrial()
            .observeOn(schedulerProvider.main())
            .subscribeFor {
                _notifyPremiumTrialResetEvent.call()
            }
    }

    private fun assertNotDebug(option: String) {
        if (!BuildConfig.DEBUG) {
            val msg = "How the hell did the '$option' option end up in Production"
            throw IllegalStateException(msg)
        }
    }

}