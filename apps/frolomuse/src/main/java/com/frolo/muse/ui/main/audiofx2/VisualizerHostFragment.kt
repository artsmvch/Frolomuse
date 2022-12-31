package com.frolo.muse.ui.main.audiofx2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.core.view.updatePadding
import com.frolo.muse.R
import com.frolo.muse.ui.base.BaseFragment
import com.frolo.muse.ui.base.FragmentContentInsetsListener
import com.frolo.muse.ui.base.setupNavigation
import com.frolo.visualizer.screen.VisualizerFeature
import kotlinx.android.synthetic.main.fragment_visualizer_host.container
import kotlinx.android.synthetic.main.fragment_visualizer_host.toolbar


class VisualizerHostFragment : BaseFragment(), FragmentContentInsetsListener {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_visualizer_host, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupNavigation(toolbar)
        ensureVisualizerFragment()
    }

    private fun ensureVisualizerFragment() {
        @IdRes val containerId = R.id.container
        val fragment = childFragmentManager.findFragmentById(containerId)
        if (fragment != null) {
            return
        }
        val newFragment = VisualizerFeature.createVisualizerFragment()
        childFragmentManager.beginTransaction()
            .replace(containerId, newFragment)
            .commitNow()
    }

    override fun applyContentInsets(left: Int, top: Int, right: Int, bottom: Int) {
        val view = this.view ?: return
        (view as? ViewGroup)?.apply {
            clipToPadding = false
            clipChildren = false
            updatePadding(left = left, top = top, right = right)
        }
        container.apply {
            clipToPadding = false
            clipChildren = false
            updatePadding(bottom = bottom)
        }
    }

    companion object {
        // Factory
        fun newInstance(): VisualizerHostFragment = VisualizerHostFragment()
    }
}