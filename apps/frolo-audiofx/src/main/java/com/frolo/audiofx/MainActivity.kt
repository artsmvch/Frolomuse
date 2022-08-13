package com.frolo.audiofx

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.frolo.audiofx.ui.control.ControlPanelFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ControlPanelFragment.newInstance().also { fragment ->
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commitNow()
        }
        // PickSessionDialog.show(supportFragmentManager)
    }
}