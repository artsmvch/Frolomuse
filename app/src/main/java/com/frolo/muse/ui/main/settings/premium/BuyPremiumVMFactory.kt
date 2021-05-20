package com.frolo.muse.ui.main.settings.premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.frolo.muse.billing.BillingManager
import com.frolo.muse.di.AppComponent
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.rx.SchedulerProvider
import javax.inject.Inject


class BuyPremiumVMFactory constructor(
    private val appComponent: AppComponent,
    private val allowTrialActivation: Boolean
) : ViewModelProvider.Factory {

    @Inject lateinit var billingManager: BillingManager
    @Inject lateinit var schedulerProvider: SchedulerProvider
    @Inject lateinit var eventLogger: EventLogger

    init {
        appComponent.inject(this)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return BuyPremiumViewModel(
            billingManager = billingManager,
            schedulerProvider = schedulerProvider,
            eventLogger = eventLogger,
            allowTrialActivation = allowTrialActivation
        ) as T
    }

}