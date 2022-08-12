package com.frolo.muse.di.modules

import com.frolo.billing.BillingManager
import com.frolo.billing.appgallery.AppGalleryBillingManagerImpl
import com.frolo.muse.FrolomuseApp
import com.frolo.muse.billing.TrialManager
import com.frolo.muse.interactor.billing.PremiumManager
import com.frolo.muse.di.ApplicationScope
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class BillingModule constructor(
    private val isDebug: Boolean
) {

    @Provides
    @ApplicationScope
    fun provideBillingManager(app: FrolomuseApp): BillingManager {
        return AppGalleryBillingManagerImpl(isDebug)
    }

    @Provides
    fun providePremiumManager(
        billingManager: BillingManager,
        trialManager: TrialManager
    ): PremiumManager {
        return PremiumManager(billingManager, trialManager)
    }

}