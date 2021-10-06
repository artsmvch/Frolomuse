package com.frolo.billing.appgallery

import com.frolo.billing.BillingManagerWrapper

// TODO: https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/introduction-0000001050033062
class AppGalleryBillingManagerImpl(
    private val isDebug: Boolean
) : BillingManagerWrapper()