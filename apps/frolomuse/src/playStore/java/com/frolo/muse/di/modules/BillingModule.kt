package com.frolo.muse.di.modules

import android.app.Activity
import android.content.Context
import com.frolo.billing.BillingManager
import com.frolo.billing.playstore.PlayStoreBillingManagerImpl
import com.frolo.billing.playstore.ForegroundActivityWatcher
import com.frolo.core.ui.ApplicationWatcher
import com.frolo.muse.FrolomuseApp
import com.frolo.muse.billing.TrialManager
import com.frolo.muse.di.ApplicationScope
import com.frolo.muse.interactor.billing.PremiumManager
import dagger.Module
import dagger.Provides


@Module
class BillingModule constructor(
    private val isDebug: Boolean
) {

    @Provides
    @ApplicationScope
    fun provideBillingManager(app: FrolomuseApp): BillingManager {
        val foregroundActivityWatcher = object : ForegroundActivityWatcher {
            override val context: Context get() = app
            override val foregroundActivity: Activity? get() =
                ApplicationWatcher.foregroundActivity
        }
        return PlayStoreBillingManagerImpl(foregroundActivityWatcher, isDebug)
    }

    @Provides
    fun providePremiumManager(
        billingManager: BillingManager,
        trialManager: TrialManager
    ): PremiumManager {
        return PremiumManager(billingManager, trialManager)
    }

}