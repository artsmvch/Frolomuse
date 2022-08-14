package com.frolo.audiofx.controlpanel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import com.frolo.audiofx.ui.R
import com.frolo.audiofx.view.EffectBoosterView
import com.frolo.audiofx2.SimpleAudioEffect2
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
        equalizer_preset_chooser.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val adapter = parent?.adapter
                if (adapter is EqualizerPresetAdapter) {
                    val item = adapter.getItem(position)
                    viewModel.onPresetClick(item)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        equalizer.observe(owner) { equalizer ->
            if (equalizer != null) {
                val shouldAnimate = equalizer_view.isLaidOut
                equalizer_view.setup(
                    equalizer = AudioFx2EqualizerToEqualizerAdapter(equalizer),
                    animate = shouldAnimate
                )
            } else {
                equalizer_view.setup(null)
            }
        }

        equalizerPresets.observe(owner) { presets ->
            equalizer_preset_chooser.adapter = EqualizerPresetAdapter(presets.orEmpty())
        }

        bassBoost.observe(owner) { bassBoost ->
            if (bassBoost != null) {
                bindAudioEffectToView(bass_booster, bassBoost)
            }
        }
    }

    private fun bindAudioEffectToView(view: EffectBoosterView, audioEffect: SimpleAudioEffect2) {
        view.onBoostValueChangeListener = EffectBoosterView.OnBoostValueChangeListener { _, boostValue ->
            val newValue: Int = audioEffect.valueRange.let { range ->
                range.minValue + ((range.maxValue - range.minValue) * boostValue).toInt()
            }
            audioEffect.value = newValue
        }
        audioEffect.addOnEffectValueChangeListener(
            SimpleAudioEffect2.OnEffectValueChangeListener { effect, value ->
                val range = effect.valueRange
                val percent: Float = (value - range.minValue).toFloat() / (range.maxValue - range.minValue)
                view.boostValue = percent
            }
        )
    }

    companion object {
        fun newInstance(): AudioFxControlPanelFragment = AudioFxControlPanelFragment()
    }
}