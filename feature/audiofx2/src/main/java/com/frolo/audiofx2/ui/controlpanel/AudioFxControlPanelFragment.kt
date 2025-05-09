package com.frolo.audiofx2.ui.controlpanel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.frolo.audiofx2.ui.AudioFx2AttachInfo
import com.frolo.audiofx2.ui.databinding.FragmentAudiofxControlPanelBinding

internal class AudioFxControlPanelFragment : Fragment() {
    private var _binding: FragmentAudiofxControlPanelBinding? = null
    private val binding: FragmentAudiofxControlPanelBinding get() = _binding!!

    private val viewModel: AudioFxControlPanelViewModel by lazy {
        ViewModelProviders.of(this).get(AudioFxControlPanelViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAudiofxControlPanelBinding.inflate(inflater)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadUi()
        observeViewModel(viewLifecycleOwner)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadUi() {
        binding.scrollView.fitsSystemWindows = true
        binding.scrollView.clipToPadding = false
        ViewCompat.setOnApplyWindowInsetsListener(binding.scrollView) { view, insets ->
            view.setPadding(insets.systemWindowInsetLeft, insets.systemWindowInsetTop,
                insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
            insets.consumeSystemWindowInsets()
        }
        binding.audioSessionDescription.setOnClickListener {
            viewModel.attachInfo.value?.also { attachInfo ->
                showAttachInfoDialog(attachInfo)
            }
        }
    }

    private fun showAttachInfoDialog(attachInfo: AudioFx2AttachInfo) {
        val dialog = AttachInfoDialog(requireContext(), attachInfo)
        dialog.show()
    }

    private fun beginDelayedTransition() {
        val transition = AutoTransition().apply {
            duration = 200L
        }
        TransitionManager.beginDelayedTransition(requireView() as ViewGroup, transition)
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        attachInfo.observe(owner) { audioSessionDescription ->
            beginDelayedTransition()
            if (audioSessionDescription != null) {
                binding.audioSessionDescription.isVisible = true
                val icon = audioSessionDescription.icon
                binding.audioSessionIcon.setImageDrawable(icon)
                binding.audioSessionIcon.isVisible = icon != null
                binding.audioSessionTitle.text = audioSessionDescription.name
            } else {
                binding.audioSessionDescription.isVisible = false
            }
        }

        equalizer.observe(owner) { equalizer ->
            beginDelayedTransition()
            binding.equalizerPanelView.setup(equalizer)
            binding.equalizerPanelView.isVisible = equalizer != null
        }

        bassBoost.observe(owner) { bassBoost ->
            beginDelayedTransition()
            binding.bassBoosterPanel.setup(bassBoost)
            binding.bassBoosterPanel.isVisible = bassBoost != null
        }

        virtualizer.observe(owner) { virtualizer ->
            beginDelayedTransition()
            binding.virtualizerPanel.setup(virtualizer)
            binding.virtualizerPanel.isVisible = virtualizer != null
        }

        loudness.observe(owner) { loudness ->
            beginDelayedTransition()
            binding.loudnessPanel.setup(loudness)
            binding.loudnessPanel.isVisible = loudness != null
        }

        reverb.observe(owner) { reverb ->
            beginDelayedTransition()
            binding.reverbPanelView.setup(reverb)
            binding.reverbPanelView.isVisible = reverb != null
        }
    }

    companion object {
        fun newInstance(): AudioFxControlPanelFragment = AudioFxControlPanelFragment()
    }
}