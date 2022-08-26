package com.frolo.audiofx2.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.frolo.audiofx.app.R
import com.frolo.audiofx2.ui.controlpanel.AudioFxControlPanelFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Frolo_AudioFx)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AudioFxControlPanelFragment.newInstance().also { fragment ->
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commitNow()
        }
        // PickSessionDialog.show(supportFragmentManager)
    }
}