package com.frolo.audiofx.di

import com.frolo.audiofx.AudioFx
import com.frolo.audiofx.AudioFxImpl


lateinit var appComponent: AppComponent
    private set

fun initAppComponent(instance: AppComponent) {
    appComponent = instance
}

interface AppComponent {
    val audioFx: AudioFxImpl
}