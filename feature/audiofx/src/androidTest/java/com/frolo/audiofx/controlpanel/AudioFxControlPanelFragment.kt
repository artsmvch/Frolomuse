package com.frolo.audiofx.controlpanel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.frolo.audiofx.R

class AudioFxControlPanelFragment : Fragment() {

    private val viewModel: AudioFxControlPanelViewModel by lazy {
        TODO("Not implemented")
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
    }
}