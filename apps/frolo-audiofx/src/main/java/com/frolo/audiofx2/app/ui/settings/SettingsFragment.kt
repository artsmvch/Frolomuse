package com.frolo.audiofx2.app.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.frolo.audiofx.app.R

internal class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.app_preferences)
        setupPreferences()
    }

    private fun setupPreferences() {
        findPreference("theme")?.apply {
            setOnPreferenceClickListener {
                showThemeChooser()
                true
            }
        }
    }

    private fun showThemeChooser() {

    }
}