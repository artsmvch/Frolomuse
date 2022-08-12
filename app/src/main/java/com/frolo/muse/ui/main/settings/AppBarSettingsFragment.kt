package com.frolo.muse.ui.main.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.frolo.ui.FragmentUtils
import com.frolo.muse.R
import com.frolo.core.ui.marker.ScrolledToTop
import com.frolo.muse.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_appbar_settings.*


// Simple wrapper for the SettingsFragment, this only puts a Toolbar at the top of it.
class AppBarSettingsFragment : BaseFragment(), ScrolledToTop {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_appbar_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tb_actions.apply {
            setTitle(R.string.nav_settings)
        }
    }

    private fun peekInnerFragment(): Fragment? {
        return childFragmentManager.findFragmentByTag("settings")
    }

    override fun scrollToTop() {
        app_bar_layout?.setExpanded(true, true)
        val innerFragment = peekInnerFragment()
        if (innerFragment is ScrolledToTop && FragmentUtils.isInForeground(innerFragment)) {
            innerFragment.scrollToTop()
        }
    }

    companion object {

        // Factory
        fun newInstance() = AppBarSettingsFragment()

    }

}
