package com.frolo.audiofx.ui.control

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.frolo.audiofx.R
import com.frolo.audiofx.di.appComponent
import com.frolo.audiofx.di.injectViewModel
import kotlinx.android.synthetic.main.fragment_control_panel.*

class ControlPanelFragment : Fragment() {

//    private val viewModel: ControlPanelViewModel by injectViewModel()

    private val viewModel: ControlPanelViewModel by lazy {
        ViewModelProviders.of(this, defaultViewModelProviderFactory)
            .get(ControlPanelViewModel::class.java)
    }

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        return ControlPanelViewModel.Factory(appComponent.audioFx)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_control_panel, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel(viewLifecycleOwner)
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        audioFx.observe(owner) { audioFx ->
            if (audioFx != null) {
                equalizer_view.setup(AudioFxToEqualizerAdapter(audioFx), animate = true)
            } else {
                equalizer_view.setup(null)
            }
        }
    }

    companion object {
        fun newInstance(): ControlPanelFragment = ControlPanelFragment()
    }
}