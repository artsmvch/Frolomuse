package com.frolo.audiofx.di

import androidx.lifecycle.MutableLiveData
import com.frolo.audiofx.AudioSessionInfo
import com.frolo.audiofx2.impl.AudioFx2Impl


lateinit var appComponent: AppComponent
    private set

fun initAppComponent(instance: AppComponent) {
    appComponent = instance
}

interface AppComponent {
    val audioFx2: AudioFx2Impl
    val audioSessionInfo: MutableLiveData<AudioSessionInfo>
}