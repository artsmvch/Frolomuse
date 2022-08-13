package com.frolo.audiofx.di

import android.app.Application
import android.widget.Toast
import com.frolo.audiofx.AudioFx
import com.frolo.audiofx.AudioFxImpl

internal class AppComponentImpl(
    private val application: Application
) : AppComponent {
    override val audioFx: AudioFxImpl by lazy {
        val errorHandler = AudioFxImpl.ErrorHandler { err ->
            Toast.makeText(application, "Error: $err", Toast.LENGTH_LONG).show()
        }
        AudioFxImpl.getInstance(application, "test", errorHandler)
    }
}