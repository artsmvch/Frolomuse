package com.frolo.muse.ui.main.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.frolo.muse.R
import com.frolo.muse.ui.base.BaseFragment
import com.frolo.muse.ui.base.NoClipping
import kotlinx.android.synthetic.main.fragment_appbar_settings.*


// Simple wrapper for the SettingsFragment, this only puts a Toolbar at the top of it.
class AppBarSettingsFragment : BaseFragment(), NoClipping {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_appbar_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as? AppCompatActivity)?.apply {
            setSupportActionBar(tb_actions as Toolbar)
            supportActionBar?.setTitle(R.string.nav_settings)
        }
    }

    override fun removeClipping(left: Int, top: Int, right: Int, bottom: Int) {
        view?.also { safeView ->
            if (safeView is ViewGroup) {
                safeView.setPadding(left, top, right, bottom)
                safeView.clipToPadding = false
            }
        }
    }

    companion object {
        fun newInstance() = AppBarSettingsFragment()
    }

}
