package com.frolo.audiofx2.app.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import com.frolo.audiofx.app.R
import com.frolo.audiofx2.app.ui.instruction.InstructionDialog
import com.frolo.audiofx2.ui.AudioFx2Feature
import com.frolo.core.ui.ApplicationWatcher
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Frolo_AudioFx)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupWindowInsets()
        addControlPanelScreen()
        maybeShowInstructions()
    }

    private fun addControlPanelScreen() {
        AudioFx2Feature.createControlPanelFragment().also { fragment ->
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commitNow()
        }
    }

    private fun setupWindowInsets() {
        skipWindowInsets(root)
        skipWindowInsets(container)
    }

    private fun skipWindowInsets(view: View) {
        view.fitsSystemWindows = true
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets -> insets }
        view.requestApplyInsets()
    }

    private fun maybeShowInstructions() {
        if (ApplicationWatcher.appStartUpInfoProvider.hotStartCount < 2) {
            val dialog = InstructionDialog(this)
            dialog.show()
        }
    }
}