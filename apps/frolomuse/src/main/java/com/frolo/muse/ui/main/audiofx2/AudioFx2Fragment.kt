package com.frolo.muse.ui.main.audiofx2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.core.view.updatePadding
import com.frolo.audiofx2.ui.AudioFx2Feature
import com.frolo.muse.BuildInfo
import com.frolo.muse.R
import com.frolo.muse.databinding.FragmentAudioFx2Binding
import com.frolo.muse.di.activityComponent
import com.frolo.muse.router.AppRouter
import com.frolo.muse.ui.base.BaseFragment
import com.frolo.muse.ui.base.FragmentContentInsetsListener
import com.frolo.muse.ui.base.setupNavigation

class AudioFx2Fragment: BaseFragment(), FragmentContentInsetsListener {
    private var _binding: FragmentAudioFx2Binding? = null
    private val binding get() = _binding!!

    // TODO: not respecting the MVVM architecture...
    private val router: AppRouter get() = activityComponent.provideAppRouter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAudioFx2Binding.inflate(inflater)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupNavigation(binding.toolbar)
        ensureControlPanelFragment()
        binding.toolbar.menu.findItem(R.id.action_playback_params)?.also { safeMenuItem ->
            safeMenuItem.setOnMenuItemClickListener {
                router.openPlaybackParams()
                true
            }
        }
        binding.toolbar.menu.findItem(R.id.action_visualizer)?.also { safeMenuItem ->
            safeMenuItem.isVisible = BuildInfo.isDebug()
            safeMenuItem.setOnMenuItemClickListener {
                router.openVisualizer()
                true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun ensureControlPanelFragment() {
        @IdRes val containerId = R.id.container
        val fragment = childFragmentManager.findFragmentById(containerId)
        if (fragment != null) {
            return
        }
        val newFragment = AudioFx2Feature.createControlPanelFragment()
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
        private const val LOG_TAG = "AudioFx2Fragment"

        // Factory
        fun newInstance(): AudioFx2Fragment = AudioFx2Fragment()
    }

}