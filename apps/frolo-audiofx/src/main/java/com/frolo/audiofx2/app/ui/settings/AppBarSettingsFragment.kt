package com.frolo.audiofx2.app.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.frolo.audiofx.app.R
import kotlinx.android.synthetic.main.fragment_appbar_settings.*

internal class AppBarSettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_appbar_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        toolbar.apply {
            setTitle(R.string.settings)
        }
    }

    private fun peekInnerFragment(): Fragment? {
        return childFragmentManager.findFragmentByTag("settings")
    }

    companion object {
        // Factory
        fun newInstance(): AppBarSettingsFragment = AppBarSettingsFragment()
    }
}
