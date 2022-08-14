package com.frolo.audiofx.di

import android.app.Application
import android.widget.Toast
import com.frolo.audiofx2.impl.AudioEffect2ErrorHandler
import com.frolo.audiofx2.impl.AudioFx2Impl

internal class AppComponentImpl(
    private val application: Application
) : AppComponent {
    override val audioFx2: AudioFx2Impl by lazy {
        val errorHandler = AudioEffect2ErrorHandler { _, err ->
            Toast.makeText(application, "Error: $err", Toast.LENGTH_LONG).show()
        }
        AudioFx2Impl.obtain(application, errorHandler).apply {
            equalizer?.isEnabled = true
            bassBoost?.isEnabled = true
        }
    }
}