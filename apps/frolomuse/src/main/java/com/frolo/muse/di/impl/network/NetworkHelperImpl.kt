package com.frolo.muse.di.impl.network

import android.content.Context
import android.net.ConnectivityManager
import com.frolo.muse.network.NetworkHelper


class NetworkHelperImpl(private val context: Context) : NetworkHelper {

    override fun isNetworkAvailable(): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager
        return manager.activeNetworkInfo.let { info ->
            info != null && info.isAvailable
        }
    }

}