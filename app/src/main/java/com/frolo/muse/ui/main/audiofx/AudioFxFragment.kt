package com.frolo.muse.ui.main.audiofx

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.CompoundButton
import androidx.core.view.doOnLayout
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.frolo.muse.R
import com.frolo.muse.StyleUtil
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.glide.GlideOptions.bitmapTransform
import com.frolo.muse.model.preset.Preset
import com.frolo.muse.model.reverb.Reverb
import com.frolo.muse.ui.Snapshots
import com.frolo.muse.ui.base.BaseFragment
import com.frolo.muse.ui.base.NoClipping
import com.frolo.muse.ui.main.audiofx.adapter.PresetAdapter
import com.frolo.muse.ui.main.audiofx.adapter.ReverbAdapter
import com.frolo.muse.ui.main.audiofx.preset.PresetSavedEvent
import com.frolo.muse.views.observeSelection
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

    private val viewModel: AudioFxViewModel by viewModel()

    private var enableStatusSwitchView: CompoundButton? = null

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

        slider_bass_boost.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                viewModel.onBassStrengthChanged(value.toShort())
            }
        }

        slider_virtualizer.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                viewModel.onVirtStrengthChanged(value.toShort())
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel(viewLifecycleOwner)
        viewModel.onOpened()
    }

    override fun onStop() {
        super.onStop()
        viewModel.onStopped()
    }

    override fun onDetach() {
        presetSaveEvent.unregister(requireContext())
        super.onDetach()
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
        sp_reverbs.observeSelection { adapterView, position ->
            val adapter = adapterView.adapter as? ReverbAdapter
            if (adapter != null) {
                val item = adapter.getItem(position)
                viewModel.onReverbSelected(item)
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

                    val windowBackground: Drawable? =
                            StyleUtil.readDrawableAttrValue(view.context, android.R.attr.windowBackground)

                    val asyncTask: AsyncTask<*, *, *> = Snapshots.makeAsync(view, windowBackground, backgroundColor) { result ->
                        Glide.with(this@AudioFxFragment)
                                .load(result)
                                .apply(bitmapTransform(BlurTransformation(10)))
                                .transition(DrawableTransitionOptions.withCrossFade(200))
                                .into(imv_blurred_snapshot)
                    }

                    saveUIAsyncTask(asyncTask)
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
            equalizer_view.setup(audioFx, true)
        }

        presets.observeNonNull(owner) { presets ->
            val adapter = PresetAdapter(presets) { item ->
                viewModel.onDeletePresetClicked(item)
            }

            sp_presets.adapter = adapter

            val selectedItem = sp_presets.getTag(R.id.tag_spinner_selected_item) as? Preset

            val selectedItemPosition = presets.indexOfFirst { it == selectedItem }

            if (selectedItemPosition >= 0 && selectedItemPosition < adapter.count) {
                sp_presets.setSelection(selectedItemPosition, false)
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
            sp_presets.setTag(R.id.tag_spinner_selected_item, preset)
        }

        bassStrengthRange.observeNonNull(owner) { range ->
            slider_bass_boost.valueFrom = range.min.toFloat()
            slider_bass_boost.valueTo = range.max.toFloat()
        }

        bassStrength.observeNonNull(owner) { strength ->
            slider_bass_boost.value = strength.toFloat()
        }

        virtStrengthRange.observeNonNull(owner) { range ->
            slider_virtualizer.valueFrom = range.min.toFloat()
            slider_virtualizer.valueTo = range.max.toFloat()
        }

        virtStrength.observeNonNull(owner) { strength ->
            slider_virtualizer.value = strength.toFloat()
        }

        reverbs.observeNonNull(owner) { reverbs ->
            val adapter = ReverbAdapter(reverbs)

            sp_reverbs.adapter = adapter

            val selectedItem = sp_reverbs.getTag(R.id.tag_spinner_selected_item) as? Reverb

            val selectedItemPosition = reverbs.indexOfFirst { it == selectedItem }

            if (selectedItemPosition >= 0 && selectedItemPosition < adapter.count) {
                sp_reverbs.setSelection(selectedItemPosition, false)
            }
        }

        selectedReverb.observeNonNull(owner) { reverb ->
            val adapter = sp_reverbs.adapter as? ReverbAdapter
            if (adapter != null) {
                val position = adapter.indexOf(reverb)
                if (position >= 0 && position < adapter.count) {
                    sp_reverbs.setSelection(position, false)
                }
            }
            sp_reverbs.setTag(R.id.tag_spinner_selected_item, reverb)
        }

        selectVisualizerRendererTypeEvent.observeNonNull(owner) { currSelectedType ->
        }
    }

    override fun removeClipping(left: Int, top: Int, right: Int, bottom: Int) {
        view?.also {
            layout_audio_fx_content?.also { safeContentLayout ->
                safeContentLayout.setPadding(left, top, right, bottom)
            }
        }
    }

    companion object {

        // Factory
        fun newInstance() = AudioFxFragment()

    }

}