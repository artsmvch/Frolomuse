package com.frolo.muse.repository


interface AppLaunchInfoProvider {
    val isFirstLaunch: Boolean
    val launchCount: Int
}