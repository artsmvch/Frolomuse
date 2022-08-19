package com.frolo.audiofx.app.engine

import android.app.Service
import android.content.Intent
import android.os.IBinder

class AudioFx2AttachEngineService : Service() {
    private val engineReceiver = AudioFx2AttachEngineReceiver()

    override fun onCreate() {
        super.onCreate()
        registerReceiver(engineReceiver, AudioFx2AttachEngineReceiver.makeIntentFilter())
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        unregisterReceiver(engineReceiver)
        super.onDestroy()
    }
}