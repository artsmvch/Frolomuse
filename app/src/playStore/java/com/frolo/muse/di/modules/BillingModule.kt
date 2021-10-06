package com.frolo.muse.di.modules

import android.app.Activity
import android.content.Context
import com.frolo.billing.BillingManager
import com.frolo.billing.playstore.BillingManagerImpl
import com.frolo.billing.playstore.ForegroundActivityWatcher
import com.frolo.muse.FrolomuseApp
import com.frolo.muse.billing.TrialManager
import com.frolo.muse.interactor.billing.PremiumManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class BillingModule constructor(
    private val isDebug: Boolean
) {

    @Provides
    @Singleton
    fun provideBillingManager(app: FrolomuseApp): BillingManager {
        val foregroundActivityWatcher = object : ForegroundActivityWatcher {
            override val context: Context get() = app
            override val foregroundActivity: Activity? get() = app.foregroundActivity
        }
        return BillingManagerImpl(foregroundActivityWatcher, isDebug)
    }

    @Provides
    fun providePremiumManager(
        billingManager: BillingManager,
        trialManager: TrialManager
    ): PremiumManager {
        return PremiumManager(billingManager, trialManager)
    }

}