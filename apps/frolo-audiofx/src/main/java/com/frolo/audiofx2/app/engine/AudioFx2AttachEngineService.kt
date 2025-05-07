package com.frolo.audiofx2.app.engine

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder

class AudioFx2AttachEngineService : Service() {
    private val engineReceiver = AudioFx2AttachEngineReceiver()

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(engineReceiver, AudioFx2AttachEngineReceiver.makeIntentFilter(),
                Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(engineReceiver, AudioFx2AttachEngineReceiver.makeIntentFilter())
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        unregisterReceiver(engineReceiver)
        super.onDestroy()
    }
}