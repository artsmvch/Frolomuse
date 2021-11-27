package com.frolo.muse.ui.main.audiofx

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.arch.*
import com.frolo.muse.engine.*
import com.frolo.muse.interactor.billing.PremiumManager
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.logCustomPresetDeleted
import com.frolo.muse.model.ShortRange
import com.frolo.muse.model.TooltipId
import com.frolo.muse.model.VisualizerRendererType
import com.frolo.muse.model.preset.CustomPreset
import com.frolo.muse.model.preset.Preset
import com.frolo.muse.model.preset.VoidPreset
import com.frolo.muse.model.reverb.Reverb
import com.frolo.muse.repository.Preferences
import com.frolo.muse.repository.PresetRepository
import com.frolo.muse.repository.TooltipManager
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.PremiumViewModel
import io.reactivex.processors.PublishProcessor
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class AudioFxViewModel @Inject constructor(
    private val player: Player,
    private val audioFx: AudioFx,
    private val schedulerProvider: SchedulerProvider,
    private val presetRepository: PresetRepository,
    private val preferences: Preferences,
    private val navigator: Navigator,
    private val premiumManager: PremiumManager,
    private val tooltipManager: TooltipManager,
    private val eventLogger: EventLogger
): PremiumViewModel(schedulerProvider, navigator, premiumManager, eventLogger) {

    private val voidPreset = presetRepository.voidPreset.blockingGet()

    private val bassBoostPublisher: PublishProcessor<Short> by lazy {
        PublishProcessor.create<Short>().also { publisher ->
            publisher
                .onBackpressureLatest()
                .debounce(200, TimeUnit.MILLISECONDS)
                .subscribeOn(schedulerProvider.worker())
                .observeOn(schedulerProvider.main())
                .subscribeFor { value ->
                    audioFx.bassStrength = value
                    audioFx.save()
                }
        }
    }

    private val virtualizerPublisher: PublishProcessor<Short> by lazy {
        PublishProcessor.create<Short>().also { publisher ->
            publisher
                .onBackpressureLatest()
                .debounce(200, TimeUnit.MILLISECONDS)
                .subscribeOn(schedulerProvider.worker())
                .observeOn(schedulerProvider.main())
                .subscribeFor { value ->
                    audioFx.virtualizerStrength = value
                    audioFx.save()
                }
        }
    }

    private val playerObserver = object : SimplePlayerObserver() {
        override fun onAudioSourceChanged(player: Player, item: AudioSource?, positionInQueue: Int) {
            _currentAudioSource.value = item
        }

        override fun onAudioSourceUpdated(player: Player, item: AudioSource) {
            _currentAudioSource.value = item
        }
    }

    private val audioFxObserver = object : AudioFxObserver {
        override fun onEnabled(audioFx: AudioFx) {
            _audioFxEnabled.value = true
        }
        override fun onDisabled(audioFx: AudioFx) {
            _audioFxEnabled.value = false
        }
        override fun onBandLevelChanged(audioFx: AudioFx, band: Short, level: Short) {
            _currentPreset.value = voidPreset
        }
        override fun onPresetUsed(audioFx: AudioFx, preset: Preset) {
            _currentPreset.value = preset
            _bandLevelsUpdate.value = BandLevelsUpdate(audioFx, true)
        }
        override fun onBassStrengthChanged(audioFx: AudioFx, strength: Short) {
            _bassStrength.value = strength
        }
        override fun onVirtualizerStrengthChanged(audioFx: AudioFx, strength: Short) {
            _virtStrength.value = strength
        }
        override fun onReverbUsed(audioFx: AudioFx, reverb: Reverb) {
            _selectedReverb.value = reverb
        }
    }

    //region LiveData members
    private val _audioSessionId = MutableLiveData<Int>()
    val audioSessionId: LiveData<Int> get() = _audioSessionId

    private val _currentAudioSource = MutableLiveData<AudioSource>(player.getCurrent())

    // Available status
    private val _equalizerAvailable = MutableLiveData<Boolean>(audioFx.hasEqualizer())
    val equalizerAvailable: LiveData<Boolean> get() = _equalizerAvailable

    private val _bassBoostAvailable = MutableLiveData<Boolean>(audioFx.hasBassBoost())
    val bassBoostAvailable: LiveData<Boolean> get() = _bassBoostAvailable

    private val _virtualizerAvailable = MutableLiveData<Boolean>(audioFx.hasVirtualizer())
    val virtualizerAvailable: LiveData<Boolean> get() = _virtualizerAvailable

    private val _presetReverbAvailable = MutableLiveData<Boolean>(audioFx.hasPresetReverbEffect())
    val presetReverbAvailable: LiveData<Boolean> get() = _presetReverbAvailable

    private val atLeastOneEffectAvailable: LiveData<Boolean> = combineMultiple(
        equalizerAvailable,
        bassBoostAvailable,
        virtualizerAvailable,
        presetReverbAvailable
    ) { values ->
        values.indexOfFirst { available -> available == true } >= 0
    }

    val screenState: LiveData<ScreenState> =
        combine(_currentAudioSource, atLeastOneEffectAvailable) { currentAudioSource, atLeastOneEffectAvailable ->
            when {
                atLeastOneEffectAvailable == false -> ScreenState.NO_EFFECTS
                currentAudioSource == null -> ScreenState.NO_AUDIO
                else -> ScreenState.NORMAL
            }
        }
        .distinctUntilChanged()

    private val _audioFxEnabled = MutableLiveData<Boolean>()
    val audioFxEnabled: LiveData<Boolean> get() = _audioFxEnabled

    private val _bandLevelsUpdate = MutableLiveData<BandLevelsUpdate>()
    val bandLevelsUpdate: LiveData<BandLevelsUpdate> get() = _bandLevelsUpdate

    private val _presets = MutableLiveData<List<Preset>>()
    val presets: LiveData<List<Preset>> get() = _presets

    private val _currentPreset = MutableLiveData<Preset>()
    val currentPreset: LiveData<Preset> get() = _currentPreset

    private val _bassStrengthRange = MutableLiveData<ShortRange>()
    val bassStrengthRange: LiveData<ShortRange> get() = _bassStrengthRange

    private val _bassStrength = MutableLiveData<Short>()
    val bassStrength: LiveData<Short> get() = _bassStrength

    private val _virtStrengthRange = MutableLiveData<ShortRange>()
    val virtStrengthRange: LiveData<ShortRange> get() = _virtStrengthRange

    private val _virtStrength = MutableLiveData<Short>()
    val virtStrength: LiveData<Short> get() = _virtStrength

    private val _reverbs = MutableLiveData<List<Reverb>>()
    val reverbs: LiveData<List<Reverb>> get() = _reverbs

    private val _selectedReverb = MutableLiveData<Reverb>()
    val selectedReverb: LiveData<Reverb> get() = _selectedReverb

    val visualizerRendererType by lazy {
        MutableLiveData<VisualizerRendererType>().apply {
            preferences.visualizerRendererType
                .observeOn(schedulerProvider.main())
                .subscribeFor { type -> value = type }
        }
    }

    private val _selectVisualizerRendererTypeEvent = SingleLiveEvent<VisualizerRendererType>()
    val selectVisualizerRendererTypeEvent: LiveData<VisualizerRendererType>
        get() = _selectVisualizerRendererTypeEvent

    private val _showTooltipEvent by lazy {
        EventLiveData<Unit>().apply {
            // We only need to show the switch tooltip if the audio fx is not enabled
            if (!audioFx.isEnabled) {
                tooltipManager.canShowTooltip(TooltipId.AUDIO_FX_SWITCH)
                    .observeOn(schedulerProvider.main())
                    .subscribeFor("can_show_audio_fx_switch_tooltip") { canShow ->
                        val state: ScreenState? = screenState.value
                        if (canShow && !audioFx.isEnabled && state == ScreenState.NORMAL) {
                            this.call()
                        }
                    }
            }
        }
    }
    val showTooltipEvent: LiveData<Unit> get() = _showTooltipEvent

    //endregion

    init {
        player.registerObserver(playerObserver)
        audioFx.registerObserver(audioFxObserver)
    }

    private fun loadPresets() {
        presetRepository.presets
            .subscribeOn(schedulerProvider.worker())
            .observeOn(schedulerProvider.computation())
            .map { customPresets ->
                val nativePresets = audioFx.nativePresets
                return@map listOf(voidPreset) + nativePresets + customPresets
            }
            .observeOn(schedulerProvider.main())
            .subscribeFor { presets -> _presets.value = presets }
    }

    /**
     * Should be called when the user interface has been created and the view model has been observed.
     * From this point, the state of the view model is synced with the state of the audio fx.
     */
    fun onUiCreated() {
        // audio session
        _audioSessionId.value = player.getAudiSessionId()
        // available status
        _equalizerAvailable.value = audioFx.hasEqualizer()
        _bassBoostAvailable.value = audioFx.hasBassBoost()
        _virtualizerAvailable.value = audioFx.hasVirtualizer()
        _presetReverbAvailable.value = audioFx.hasPresetReverbEffect()
        // enabled status
        _audioFxEnabled.value = audioFx.isEnabled
        // equalizer
        _bandLevelsUpdate.value = BandLevelsUpdate(audioFx, false)
        // preset
        _currentPreset.value = audioFx.currentPreset ?: voidPreset
        // bass
        _bassStrengthRange.value =
                ShortRange.of(audioFx.minBassStrength, audioFx.maxBassStrength)
        _bassStrength.value = audioFx.bassStrength
        // virt
        _virtStrengthRange.value =
                ShortRange.of(audioFx.minVirtualizerStrength, audioFx.maxVirtualizerStrength)
        _virtStrength.value = audioFx.virtualizerStrength
        // preset reverb
        _reverbs.value = audioFx.reverbs
        _selectedReverb.value = audioFx.currentReverb

        loadPresets()
    }

    fun onSwitchTooltipShown() {
        tooltipManager.markTooltipShown(TooltipId.AUDIO_FX_SWITCH)
            .observeOn(schedulerProvider.main())
            .subscribeFor {  }
    }

    fun onStopped() {
        audioFx.save()
    }

    fun onPresetSaved(preset: CustomPreset) {
        audioFx.usePreset(preset)
        loadPresets()
    }

    fun onDeletePresetClicked(preset: CustomPreset) {
        presetRepository.delete(preset)
            .subscribeOn(schedulerProvider.worker())
            .observeOn(schedulerProvider.main())
            .doOnComplete { eventLogger.logCustomPresetDeleted() }
            .subscribeFor {
                audioFx.unusePreset()
                loadPresets()
            }
    }

    fun onEnableStatusChanged(enabled: Boolean) {
        audioFx.isEnabled = enabled
    }

    fun onPresetSelected(preset: Preset) {
        when (preset) {
            is VoidPreset -> audioFx.unusePreset()
            else -> audioFx.usePreset(preset)
        }
    }

    fun onReverbSelected(item: Reverb) {
        audioFx.useReverb(item)
    }

    fun onBassStrengthChanged(strength: Short) {
        bassBoostPublisher.onNext(strength)
    }

    fun onVirtStrengthChanged(strength: Short) {
        virtualizerPublisher.onNext(strength)
    }

    fun onPlaybackParamsOptionSelected() {
        navigator.openPlaybackParams()
    }

    fun onSavePresetButtonClicked(currentBandLevels: ShortArray) {
        navigator.savePreset(currentBandLevels)
    }

    fun onVisualizerRendererTypeOptionClicked() {
        val currType = visualizerRendererType.value ?: return
        _selectVisualizerRendererTypeEvent.value = currType
    }

    fun onVisualizerRendererTypeSelected(type: VisualizerRendererType) {
        preferences.setVisualizerRendererType(type)
            .subscribeOn(schedulerProvider.worker())
            .observeOn(schedulerProvider.main())
            .subscribeFor {
            }
    }

    override fun onCleared() {
        super.onCleared()
        player.unregisterObserver(playerObserver)
        audioFx.unregisterObserver(audioFxObserver)
        audioFx.save()
    }

    enum class ScreenState {
        NORMAL, NO_AUDIO, NO_EFFECTS
    }

    class BandLevelsUpdate(val audioFx: AudioFx, val animate: Boolean)

}