package com.frolo.audiofx2.ui.controlpanel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.frolo.audiofx2.ui.AudioFx2AttachInfo
import com.frolo.audiofx2.ui.R
import kotlinx.android.synthetic.main.fragment_audiofx_control_panel.*

class AudioFxControlPanelFragment : Fragment() {

    private val viewModel: AudioFxControlPanelViewModel by lazy {
        ViewModelProviders.of(this).get(AudioFxControlPanelViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_audiofx_control_panel, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadUi()
        observeViewModel(viewLifecycleOwner)
    }

    private fun loadUi() {
        audio_session_description.setOnClickListener {
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
                audio_session_description.isVisible = true
                val icon = audioSessionDescription.icon
                audio_session_icon.setImageDrawable(icon)
                audio_session_icon.isVisible = icon != null
                audio_session_title.text = audioSessionDescription.name
            } else {
                audio_session_description.isVisible = false
            }
        }

        equalizer.observe(owner) { equalizer ->
            beginDelayedTransition()
            equalizer_panel_view.setup(equalizer)
            equalizer_panel_view.isVisible = equalizer != null
        }

        bassBoost.observe(owner) { bassBoost ->
            beginDelayedTransition()
            bass_booster_panel.setup(bassBoost)
            bass_booster_panel.isVisible = bassBoost != null
        }

        virtualizer.observe(owner) { virtualizer ->
            beginDelayedTransition()
            virtualizer_panel.setup(virtualizer)
            virtualizer_panel.isVisible = virtualizer != null
        }

        loudness.observe(owner) { loudness ->
            beginDelayedTransition()
            loudness_panel.setup(loudness)
            loudness_panel.isVisible = loudness != null
        }

        reverb.observe(owner) { reverb ->
            beginDelayedTransition()
            reverb_panel_view.setup(reverb)
            reverb_panel_view.isVisible = reverb != null
        }
    }

    companion object {
        fun newInstance(): AudioFxControlPanelFragment = AudioFxControlPanelFragment()
    }
}