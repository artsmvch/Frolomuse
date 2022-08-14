package com.frolo.audiofx

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.frolo.audiofx.controlpanel.AudioFxControlPanelFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
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