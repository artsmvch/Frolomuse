package com.frolo.muse.ui.main.settings.premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.frolo.muse.di.ActivityComponentInjector
import com.frolo.muse.interactor.billing.PremiumManager
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.rx.SchedulerProvider
import javax.inject.Inject


class BuyPremiumVMFactory constructor(
    private val injector: ActivityComponentInjector,
    private val allowTrialActivation: Boolean
) : ViewModelProvider.Factory {

    @Inject lateinit var premiumManager: PremiumManager
    @Inject lateinit var schedulerProvider: SchedulerProvider
    @Inject lateinit var eventLogger: EventLogger

    init {
        injector.inject(this)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BuyPremiumViewModel(
            premiumManager = premiumManager,
            schedulerProvider = schedulerProvider,
            eventLogger = eventLogger,
            allowTrialActivation = allowTrialActivation
        ) as T
    }

}