package com.frolo.muse.ui.main.audiofx2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.updatePadding
import com.frolo.audiofx.controlpanel.AudioFxControlPanelFragment
import com.frolo.muse.ui.base.BaseFragment
import com.frolo.muse.ui.base.FragmentContentInsetsListener

class AudioFx2Fragment: BaseFragment(), FragmentContentInsetsListener {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FrameLayout(inflater.context).apply { id = containerId }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val fragment = childFragmentManager.findFragmentById(containerId)
        if (fragment != null && fragment is AudioFxControlPanelFragment) {
            return
        }
        val newFragment = AudioFxControlPanelFragment.newInstance()
        childFragmentManager.beginTransaction()
            .replace(containerId, newFragment)
            .commitNow()
    }

    override fun applyContentInsets(left: Int, top: Int, right: Int, bottom: Int) {
        val view = this.view ?: return
        if (view is ViewGroup) {
            view.clipToPadding = false
        }
        view.updatePadding(left, top, right, bottom)
    }

    companion object {
        private const val LOG_TAG = "AudioFx2Fragment"

        private val containerId: Int = View.generateViewId()

        // Factory
        fun newInstance(): AudioFx2Fragment = AudioFx2Fragment()
    }

}