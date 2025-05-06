package com.frolo.muse.ui.main.audiofx2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.core.view.updatePadding
import com.frolo.muse.R
import com.frolo.muse.databinding.FragmentVisualizerHostBinding
import com.frolo.muse.ui.base.BaseFragment
import com.frolo.muse.ui.base.FragmentContentInsetsListener
import com.frolo.muse.ui.base.setupNavigation
import com.frolo.visualizer.screen.VisualizerFeature


class VisualizerHostFragment : BaseFragment(), FragmentContentInsetsListener {
    private var _binding: FragmentVisualizerHostBinding? = null
    private val binding: FragmentVisualizerHostBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVisualizerHostBinding.inflate(inflater)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupNavigation(binding.toolbar)
        ensureVisualizerFragment()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
        binding.container.apply {
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