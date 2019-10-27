package com.frolo.muse.di.modules

import android.content.Context
import com.frolo.muse.di.impl.network.NetworkHelperImpl
import com.frolo.muse.network.NetworkHelper
import dagger.Module
import dagger.Provides


@Module
class NetworkModule {

    @Provides
    fun provideNetworkHelper(context: Context): NetworkHelper {
        return NetworkHelperImpl(context)
    }

}