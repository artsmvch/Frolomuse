package com.frolo.muse.ui.main.audiofx

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Size
import android.view.*
import android.widget.CompoundButton
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.frolo.muse.Logger
import com.frolo.muse.R
import com.frolo.muse.StyleUtil
import com.frolo.muse.arch.observe
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.dp2px
import com.frolo.muse.glide.GlideOptions.bitmapTransform
import com.frolo.muse.model.preset.Preset
import com.frolo.muse.model.reverb.Reverb
import com.frolo.muse.ui.ProBadgedDrawable
import com.frolo.muse.ui.Snapshots
import com.frolo.muse.ui.base.BaseFragment
import com.frolo.muse.ui.base.NoClipping
import com.frolo.muse.ui.main.audiofx.adapter.PresetAdapter
import com.frolo.muse.ui.main.audiofx.adapter.ReverbAdapter
import com.frolo.muse.ui.main.audiofx.preset.PresetSavedEvent
import com.frolo.muse.views.observeSelection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.fragment_audio_fx.*
import kotlinx.android.synthetic.main.include_audio_fx_content.*
import kotlinx.android.synthetic.main.include_audio_fx_content_lock.*
import kotlinx.android.synthetic.main.include_preset_chooser.*
import kotlinx.android.synthetic.main.include_preset_reverb_chooser.*
import kotlinx.android.synthetic.main.include_seekbar_bass_boost.*
import kotlinx.android.synthetic.main.include_seekbar_visualizer.*


class AudioFxFragment: BaseFragment(), NoClipping {

    private val viewModel: AudioFxViewModel by viewModel()

    private var enableStatusSwitchView: CompoundButton? = null

    /**
     * Listener for [enableStatusSwitchView].
     */
    private val onStatusCheckedChangeListener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { _, isChecked ->
            viewModel.onEnableStatusChanged(isChecked)
        }

    private lateinit var presetSaveEvent: PresetSavedEvent

    private var blurredSnapshotDisposable: Disposable? = null

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

                // Here, we configure the switch.

                // The listener
                switchView.setOnCheckedChangeListener(onStatusCheckedChangeListener)

                // The next line disables the save-restore mechanism for this switch view.
                // So that, [onStatusCheckedChangeListener] callback will not be fired
                // when restoring views in this fragment.
                switchView.isSaveEnabled = false

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

        // making the content under the overlay not clickable
        fl_overlay.setOnClickListener { /* no-op */ }
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

    override fun onDestroyView() {
        blurredSnapshotDisposable?.dispose()
        super.onDestroyView()
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

        isPlaybackParamsProBadged.observeNonNull(owner) { isBadged ->
            tb_actions.menu.findItem(R.id.action_playback_params)?.also { menuItem ->
                val context = requireContext()
                val icon = AppCompatResources.getDrawable(context, R.drawable.ic_playback_params)
                if (isBadged && icon != null) {
                    menuItem.icon = ProBadgedDrawable(context, icon, rightMargin = 6f.dp2px(context).toInt())
                } else {
                    menuItem.icon = icon
                }
            }
        }

        screenState.observe(owner) { state ->
            (view as? ViewGroup)?.also { viewGroup ->
                val transition = AutoTransition().apply {
                    duration = 200L
                    addTarget(fl_overlay)
                }
                TransitionManager.beginDelayedTransition(viewGroup, transition)
            }
            when (state) {
                AudioFxViewModel.ScreenState.NO_EFFECTS -> {
                    fl_overlay.isVisible = true
                    tv_overlay_text.setText(R.string.no_audio_effects_available)
                    tv_overlay_description.setText(R.string.no_audio_effects_available_desc)
                }
                AudioFxViewModel.ScreenState.NO_AUDIO -> {
                    fl_overlay.isVisible = true
                    tv_overlay_text.setText(R.string.no_audio_is_playing_now)
                    tv_overlay_description.setText(R.string.no_audio_is_playing_now_desc)
                }
                AudioFxViewModel.ScreenState.NORMAL, null -> {
                    fl_overlay.isVisible = false
                }
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

            enableStatusSwitchView?.apply {
                // Before setting the check flag, we need to set the listener to null.
                // Then set the needed checked flag.
                // Finally, set the actual listener back.
                // Thus, we avoid dispatching unnecessary events about checked flag changes.
                setOnCheckedChangeListener(null)
                isChecked = enabled
                setOnCheckedChangeListener(onStatusCheckedChangeListener)
            }

            layout_audio_fx_content.isEnabled = enabled

            // Always dispose old blurred snapshot source to prevent OOM errors
            blurredSnapshotDisposable?.dispose()

            if (enabled) {
                inc_audio_fx_content_lock.animate()
                    .alpha(0.0f)
                    .setDuration(300L)
                    .withEndAction {
                        inc_audio_fx_content_lock?.visibility = View.INVISIBLE
                    }
                    .start()
            } else {
                imv_blurred_snapshot.setImageDrawable(null)

                layout_audio_fx_content.doOnLayout { view ->

                    val backgroundColor: Int =
                            StyleUtil.resolveColor(view.context, R.attr.colorSurface)

                    val windowBackground: Drawable? =
                            StyleUtil.resolveDrawable(view.context, android.R.attr.windowBackground)

                    // Calculating optimal size for the snapshot
                    // so it will be loaded faster and will consume not that much memory
                    val targetWidth = 600
                    val targetHeight = (targetWidth * (view.measuredHeight.toFloat() / view.measuredWidth)).toInt()
                    val targetSize = Size(targetWidth, targetHeight)

                    blurredSnapshotDisposable = Snapshots.make(view, windowBackground, backgroundColor, targetSize)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            { result ->
                                Glide.with(this@AudioFxFragment)
                                    .load(result)
                                    .apply(bitmapTransform(BlurTransformation(10)))
                                    .transition(DrawableTransitionOptions.withCrossFade(200))
                                    .into(imv_blurred_snapshot)
                            },
                            { err ->
                                Logger.e(LOG_TAG, err)
                            }
                        )

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

        bandLevelsUpdate.observeNonNull(owner) { bandLevelsUpdate ->
            equalizer_view.setup(bandLevelsUpdate.audioFx, bandLevelsUpdate.animate)
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

        private const val LOG_TAG = "AudioFxFragment"

        // Factory
        fun newInstance() = AudioFxFragment()

    }

}