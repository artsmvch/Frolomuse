package com.frolo.muse.ui.main.settings


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.frolo.muse.R
import com.frolo.muse.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_appbar_settings.*


// Additional toolbar above settings fragment
class AppBarSettingsFragment : BaseFragment() {

    companion object {
        fun newInstance() = AppBarSettingsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_appbar_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.apply {
            setSupportActionBar(tb_actions as Toolbar)
            supportActionBar?.setTitle(R.string.nav_settings)
        }
    }
}
