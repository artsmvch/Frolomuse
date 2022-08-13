package com.frolo.audiofx.controlpanel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
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
        audioFx.observe(owner) { audioFx ->
            if (audioFx != null) {
                val shouldAnimate = equalizer_view.isLaidOut
                equalizer_view.setup(
                    equalizer = AudioFxToEqualizerAdapter(audioFx),
                    animate = shouldAnimate
                )
            } else {
                equalizer_view.setup(null)
            }
        }
    }

    companion object {
        fun newInstance(): AudioFxControlPanelFragment = AudioFxControlPanelFragment()
    }
}