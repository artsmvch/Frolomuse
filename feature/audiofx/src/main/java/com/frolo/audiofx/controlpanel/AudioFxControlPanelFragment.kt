package com.frolo.audiofx.controlpanel

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
import com.frolo.audiofx.ui.R
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
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        audioSessionDescriptor.observe(owner) { audioSessionDescription ->
            val transition = AutoTransition().apply {
                duration = 200L
            }
            TransitionManager.beginDelayedTransition(requireView() as ViewGroup, transition)
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
            equalizer_panel_view.setup(equalizer)
        }

        bassBoost.observe(owner) { bassBoost ->
            if (bassBoost != null) {
                bass_booster_panel.setup(bassBoost)
            }
        }

        virtualizer.observe(owner) { bassBoost ->
            if (bassBoost != null) {
                virtualizer_panel.setup(bassBoost)
            }
        }

        loudness.observe(owner) { loudness ->
            if (loudness != null) {
                loudness_panel.setup(loudness)
            }
        }
    }

    companion object {
        fun newInstance(): AudioFxControlPanelFragment = AudioFxControlPanelFragment()
    }
}