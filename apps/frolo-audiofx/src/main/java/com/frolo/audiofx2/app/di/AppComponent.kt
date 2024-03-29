package com.frolo.audiofx2.app.di

import androidx.lifecycle.MutableLiveData
import com.frolo.audiofx2.ui.AudioFx2AttachInfo
import com.frolo.audiofx2.app.engine.AudioFx2AttachEngine
import com.frolo.audiofx2.impl.AudioFx2Impl


lateinit var appComponent: AppComponent
    private set

fun initAppComponent(instance: AppComponent) {
    appComponent = instance
}

interface AppComponent {
    val audioFx2: AudioFx2Impl
    val audioFx2AttachInfo: MutableLiveData<AudioFx2AttachInfo>
    val attachEngine: AudioFx2AttachEngine
}