package com.frolo.muse.ui.main.audiofx

import android.Manifest
import android.content.Context
import android.media.audiofx.Visualizer
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.CompoundButton
import android.widget.SeekBar
import androidx.core.view.doOnLayout
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.frolo.muse.R
import com.frolo.muse.StyleUtil
import com.frolo.muse.Trace
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.glide.GlideOptions.bitmapTransform
import com.frolo.muse.ui.Snapshots
import com.frolo.muse.ui.base.BaseFragment
import com.frolo.muse.ui.base.NoClipping
import com.frolo.muse.ui.main.audiofx.adapter.PresetAdapter
import com.frolo.muse.ui.main.audiofx.adapter.PresetReverbAdapter
import com.frolo.muse.ui.main.audiofx.preset.PresetSavedEvent
import com.frolo.muse.views.equalizer.EqualizerLayout
import com.frolo.muse.views.observeSelection
import com.frolo.muse.views.visualizer.SpectrumRenderer
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.fragment_audio_fx.*
import kotlinx.android.synthetic.main.include_audio_fx_content.*
import kotlinx.android.synthetic.main.include_audio_fx_content_lock.*
import kotlinx.android.synthetic.main.include_message.*
import kotlinx.android.synthetic.main.include_preset_chooser.*
import kotlinx.android.synthetic.main.include_preset_reverb_chooser.*
import kotlinx.android.synthetic.main.include_seekbar_bass_boost.*
import kotlinx.android.synthetic.main.include_seekbar_visualizer.*


class AudioFxFragment: BaseFragment(), NoClipping {

    companion object {

        // Factory
        fun newInstance() = AudioFxFragment()
    }

    private val viewModel: AudioFxViewModel by viewModel()

    private var enableStatusSwitchView: CompoundButton? = null
    private var visualizer: Visualizer? = null

    private lateinit var presetSaveEvent: PresetSavedEvent

    override fun onAttach(context: Context) {
        super.onAttach(context)
        presetSaveEvent = PresetSavedEvent.register(context) { preset ->
            viewModel.onPresetSaved(preset)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_audio_fx, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        tb_actions.apply {
            setTitle(R.string.nav_equalizer)

            inflateMenu(R.menu.fragment_audio_fx)

            menu.findItem(R.id.action_playback_params)?.also { safeMenuItem ->
                // this option is available only for Android API versions M (Marshmallow) and higher
                safeMenuItem.isVisible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

                safeMenuItem.setOnMenuItemClickListener {
                    viewModel.onPlaybackParamsOptionSelected()
                    true
                }
            }

            menu.findItem(R.id.action_switch_audio_fx)?.also { safeMenuItem ->
                val switchView = safeMenuItem.actionView as CompoundButton
                switchView.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.onEnableStatusChanged(isChecked)
                }
                enableStatusSwitchView = switchView
            }
        }

        initPresetChooser()

        initPresetReverbChooser()

        initBassBoostBar()

        initVirtualizer()

        initVisualizer()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel(viewLifecycleOwner)
        viewModel.onOpened()
    }

    override fun onDestroyView() {
        visualizer?.also { safeVisualizer ->
            safeVisualizer
                .runCatching { release() }
                .onFailure { Trace.e(it) }
        }
        visualizer = null
        super.onDestroyView()
    }

    override fun onDetach() {
        presetSaveEvent.unregister(requireContext())
        super.onDetach()
    }

    private fun showWaveForm() {
        visualizer_view.visibility = View.VISIBLE

        val context = visualizer_view.context
        //val extinctColor = ContextCompat.getColor(context, R.color.ghost)
        val gainedColor = StyleUtil.getVisualizerColor(context)

        visualizer_view.renderer = SpectrumRenderer().apply {
            color = gainedColor
        }

        runCatching {
            if (visualizer == null) {
                visualizer = viewModel.audioSessionId.value?.let { Visualizer(it) }
            }
        }.onFailure {
            Trace.e(it)
            toastError(it)
        }

        visualizer?.also { safeVisualizer ->
            safeVisualizer.runCatching {
                captureSize = Visualizer.getCaptureSizeRange()[0]
                setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                    override fun onFftDataCapture(visualizer: Visualizer?, fft: ByteArray?, samplingRate: Int) {
                        // we ignore fft
                    }
                    override fun onWaveFormDataCapture(visualizer: Visualizer?, waveform: ByteArray?, samplingRate: Int) {
                        visualizer_view?.setData(waveform)
                    }
                }, Visualizer.getMaxCaptureRate(), true, false)
                enabled = true
            }.onFailure {
                Trace.e(it)
            }
        }
    }

    private fun initVisualizer() {
        btn_show_visualizer.setOnClickListener {
            requestRxPermissions(Manifest.permission.RECORD_AUDIO) { granted ->
                if (granted) {
                    btn_show_visualizer.visibility = View.GONE
                    showWaveForm()
                }
            }
        }

        if (isPermissionGranted(Manifest.permission.RECORD_AUDIO)) {
            btn_show_visualizer.visibility = View.GONE
            showWaveForm()
        }
    }

    private fun initBassBoostBar() {
        sb_bass_boost.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                viewModel.onBassStrengthChanged(seekBar.progress.toShort())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
        })
    }

    private fun initVirtualizer() {
        sb_virtualizer.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                viewModel.onVirtStrengthChanged(seekBar.progress.toShort())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
        })
    }

    private fun initPresetChooser() {
        sp_presets.observeSelection { adapterView, position ->
            val adapter = adapterView.adapter as? PresetAdapter
            if (adapter != null) {
                val item = adapter.getItem(position)
                viewModel.onPresetSelected(item)
            }
        }

        btn_save_preset.setOnClickListener {
            val currentBandLevels = equalizer_view.currentLevels
            viewModel.onSavePresetButtonClicked(currentBandLevels)
        }
    }

    private fun initPresetReverbChooser() {
        sp_preset_reverbs.observeSelection { adapterView, position ->
            val adapter = adapterView.adapter as? PresetReverbAdapter
            if (adapter != null) {
                val item = adapter.getItem(position)
                viewModel.onPresetReverbSelected(item.first)
            }
        }
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        error.observeNonNull(owner) { err ->
            toastError(err)
        }

        audioFxAvailable.observeNonNull(owner) { available ->
            if (available) {
                layout_player_placeholder.visibility = View.GONE
            } else {
                tv_message.setText(R.string.current_playlist_is_empty)
                layout_player_placeholder.setOnClickListener { } // to make all view under overlay non-clickable
                layout_player_placeholder.visibility = View.VISIBLE
            }
        }

        equalizerAvailable.observeNonNull(owner) { available ->
            equalizer_view.visibility = if (available) View.VISIBLE else View.GONE
            ll_preset_chooser.visibility = if (available) View.VISIBLE else View.GONE
        }

        bassBoostAvailable.observeNonNull(owner) { available ->
            ll_bass_boost.visibility = if (available) View.VISIBLE else View.GONE
        }

        virtualizerAvailable.observeNonNull(owner) { available ->
            ll_virtualizer.visibility = if (available) View.VISIBLE else View.GONE
        }

        presetReverbAvailable.observeNonNull(owner) { available ->
            ll_preset_reverb_chooser.visibility = if (available) View.VISIBLE else View.GONE
        }

        audioFxEnabled.observeNonNull(owner) { enabled ->
            enableStatusSwitchView?.isChecked = enabled

            layout_audio_fx_content.isEnabled = enabled

            if (enabled) {
                inc_audio_fx_content_lock.animate()
                    .alpha(0.0f)
                    .setDuration(300L)
                    .withEndAction {
                        inc_audio_fx_content_lock?.visibility = View.INVISIBLE
                    }
                    .start()
            } else {
                layout_audio_fx_content.doOnLayout { view ->
                    val backgroundColor: Int =
                            StyleUtil.readColorAttrValue(view.context, R.attr.colorSurface)
                    val snapshot = Snapshots.make(view, backgroundColor)
                    Glide.with(this@AudioFxFragment)
                        .load(snapshot)
                        .apply(bitmapTransform(BlurTransformation(5)))
                        .transition(DrawableTransitionOptions.withCrossFade(200))
                        .into(imv_blurred_snapshot)
                }

                inc_audio_fx_content_lock.animate()
                    .alpha(1.0f)
                    .setDuration(300L)
                    .withStartAction {
                        inc_audio_fx_content_lock?.visibility = View.VISIBLE
                    }
                    .start()
            }
        }

        bandLevels.observeNonNull(owner) { audioFx ->
            val equalizerProvider = AudioFxEqualizerProvider(audioFx)
            equalizer_view.setup(equalizerProvider, true)
        }

        presets.observeNonNull(owner) { presets ->
            val adapter = PresetAdapter(presets) { item ->
                onDeletePresetClicked(item)
            }
            sp_presets.adapter = adapter
            val position = viewModel.currentPreset.value?.let { adapter.indexOf(it) } ?: -1
            if (position >= 0 && position < adapter.count) {
                sp_presets.setSelection(position, false)
            }
        }

        currentPreset.observeNonNull(owner) { preset ->
            val adapter = sp_presets.adapter as? PresetAdapter
            if (adapter != null) {
                val position = adapter.indexOf(preset)
                if (position >= 0 && position < adapter.count) {
                    sp_presets.setSelection(position, false)
                }
            }
        }

        bassStrengthRange.observeNonNull(owner) { range ->
            sb_bass_boost.max = range.second.toInt()
        }

        bassStrength.observeNonNull(owner) { strength ->
            sb_bass_boost.progress = strength.toInt()
        }

        virtStrengthRange.observeNonNull(owner) { range ->
            sb_virtualizer.max = range.second.toInt()
        }

        virtStrength.observeNonNull(owner) { strength ->
            sb_virtualizer.progress = strength.toInt()
        }

        presetReverbs.observeNonNull(owner) { reverbs ->
            sp_preset_reverbs.adapter = PresetReverbAdapter(reverbs)
            val position = presetReverbIndex.value.let { index ->
                reverbs.indexOfFirst { it.first == index }
            }
            sp_preset_reverbs.setSelection(position, false)
        }

        presetReverbIndex.observeNonNull(owner) { index ->

        }
    }

    override fun removeClipping(left: Int, top: Int, right: Int, bottom: Int) {
        view?.also { safeView ->
            if (safeView is ViewGroup) {
                safeView.setPadding(left, top, right, bottom)
                safeView.clipToPadding = false
            }
        }
    }

    private fun setAudioFxEnabledInternal(enabled: Boolean) {

    }
}