package com.frolo.muse.ui.main.audiofx

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.engine.AudioFx
import com.frolo.muse.engine.AudioFxObserver
import com.frolo.muse.engine.Player
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.preset.CustomPreset
import com.frolo.muse.model.preset.NativePreset
import com.frolo.muse.model.preset.Preset
import com.frolo.muse.model.preset.VoidPreset
import com.frolo.muse.repository.PresetRepository
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseViewModel
import io.reactivex.processors.PublishProcessor
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class AudioFxViewModel @Inject constructor(
        private val player: Player,
        private val audioFx: AudioFx,
        private val schedulerProvider: SchedulerProvider,
        private val repository: PresetRepository,
        private val navigator: Navigator,
        private val eventLogger: EventLogger
): BaseViewModel(eventLogger) {

    private val voidPreset = repository.voidPreset.blockingGet()

    // Publishers
    private val bandLevelPublisher: PublishProcessor<Pair<Short, Short>> by lazy {
        PublishProcessor.create<Pair<Short, Short>>().also { publisher ->
            publisher
                    .debounce(300, TimeUnit.MILLISECONDS)
                    .onBackpressureLatest()
                    .subscribeOn(schedulerProvider.worker())
                    .observeOn(schedulerProvider.main())
                    .subscribeFor { pair ->
                        audioFx.setBandLevel(pair.first, pair.second)
                        audioFx.save()
                    }
        }
    }

    private val bassBoostPublisher: PublishProcessor<Short> by lazy {
        PublishProcessor.create<Short>().also { publisher ->
            publisher
                    .debounce(200, TimeUnit.MILLISECONDS)
                    .onBackpressureLatest()
                    .subscribeOn(schedulerProvider.worker())
                    .observeOn(schedulerProvider.main())
                    .subscribeFor { value ->
                        audioFx.setBassStrength(value)
                        audioFx.save()
                    }
        }
    }

    private val virtualizerPublisher: PublishProcessor<Short> by lazy {
        PublishProcessor.create<Short>().also { publisher ->
            publisher
                    .debounce(200, TimeUnit.MILLISECONDS)
                    .onBackpressureLatest()
                    .subscribeOn(schedulerProvider.worker())
                    .observeOn(schedulerProvider.main())
                    .subscribeFor { value ->
                        audioFx.setVirtualizerStrength(value)
                        audioFx.save()
                    }
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
            _bandLevels.value = audioFx
        }
        override fun onBassStrengthChanged(audioFx: AudioFx, strength: Short) {
            _bassStrength.value = strength
        }
        override fun onVirtualizerStrengthChanged(audioFx: AudioFx, strength: Short) {
            _virtStrength.value = strength
        }
        override fun onPresetReverbUsed(audioFx: AudioFx, presetReverbIndex: Short) {
            _presetReverbIndex.value = presetReverbIndex
        }
    }

    private val _audioSessionId: MutableLiveData<Int> = MutableLiveData()
    val audioSessionId: LiveData<Int> = _audioSessionId

    // Available status
    private val _audioFxAvailable: MutableLiveData<Boolean> = MutableLiveData()
    val audioFxAvailable: LiveData<Boolean> = _audioFxAvailable

    private val _equalizerAvailable: MutableLiveData<Boolean> = MutableLiveData()
    val equalizerAvailable: LiveData<Boolean> = _equalizerAvailable

    private val _bassBoostAvailable: MutableLiveData<Boolean> = MutableLiveData()
    val bassBoostAvailable: LiveData<Boolean> = _bassBoostAvailable

    private val _virtualizerAvailable: MutableLiveData<Boolean> = MutableLiveData()
    val virtualizerAvailable: LiveData<Boolean> = _virtualizerAvailable

    private val _presetReverbAvailable: MutableLiveData<Boolean> = MutableLiveData()
    val presetReverbAvailable: LiveData<Boolean> = _presetReverbAvailable

    // Enabled status
    private val _audioFxEnabled: MutableLiveData<Boolean> = MutableLiveData()
    val audioFxEnabled: LiveData<Boolean> = _audioFxEnabled

    // Equalizer
    private val _bandLevels: MutableLiveData<AudioFx> = MutableLiveData()
    val bandLevels: LiveData<AudioFx> = _bandLevels

    private val _presets: MutableLiveData<List<Preset>> = MutableLiveData()
    val presets: LiveData<List<Preset>> = _presets

    private val _currentPreset: MutableLiveData<Preset> = MutableLiveData()
    val currentPreset: LiveData<Preset> = _currentPreset

    private val _bassStrengthRange: MutableLiveData<Pair<Short, Short>> = MutableLiveData()
    val bassStrengthRange: LiveData<Pair<Short, Short>> = _bassStrengthRange

    private val _bassStrength: MutableLiveData<Short> = MutableLiveData()
    val bassStrength: LiveData<Short> = _bassStrength

    private val _virtStrengthRange: MutableLiveData<Pair<Short, Short>> = MutableLiveData()
    val virtStrengthRange: LiveData<Pair<Short, Short>> = _virtStrengthRange

    private val _virtStrength: MutableLiveData<Short> = MutableLiveData()
    val virtStrength: LiveData<Short> = _virtStrength

    private val _presetReverbs: MutableLiveData<List<Pair<Short, String>>> = MutableLiveData()
    val presetReverbs: LiveData<List<Pair<Short, String>>> = _presetReverbs

    private val _presetReverbIndex: MutableLiveData<Short> = MutableLiveData()
    val presetReverbIndex: LiveData<Short> = _presetReverbIndex

    init {
        audioFx.registerObserver(audioFxObserver)
        onOpened()
    }

    private fun loadPresets() {
        repository.presets
                .map { customPresets ->
                    val nativePresets: MutableList<Preset> = (0 until audioFx.getNumberOfPresets())
                            .map { index -> NativePreset(index.toShort(), audioFx.getPresetName(index.toShort())) }
                            .toMutableList()
                    return@map listOf(voidPreset) + nativePresets + customPresets
                }
                .subscribeFor(schedulerProvider) { presets ->
                    _presets.value = presets
                }
    }

    fun onOpened() {
        // audio session
        _audioSessionId.value = player.getAudiSessionId()
        // available status
        _audioFxAvailable.value = player.getCurrent() != null
        _equalizerAvailable.value = audioFx.hasEqualizer()
        _bassBoostAvailable.value = audioFx.hasBassBoost()
        _virtualizerAvailable.value = audioFx.hasVirtualizer()
        _presetReverbAvailable.value = audioFx.hasPresetReverb()
        // enabled status
        _audioFxEnabled.value = audioFx.isEnabled()
        // equalizer
        _bandLevels.value = audioFx
        // preset
        _currentPreset.value = when {
            audioFx.isUsingCustomPreset() -> audioFx.getCurrentCustomPreset()
            audioFx.isUsingNativePreset() -> audioFx.getCurrentNativePreset()
            else -> voidPreset
        }
        // bass
        _bassStrengthRange.value = audioFx.getMinBassStrength() to audioFx.getMaxBassStrength()
        _bassStrength.value = audioFx.getBassStrength()
        // virt
        _virtStrengthRange.value = audioFx.getMinVirtualizerStrength() to audioFx.getMaxVirtualizerStrength()
        _virtStrength.value = audioFx.getVirtualizerStrength()
        // preset reverb
        _presetReverbs.value = audioFx.getPresetReverbIndexes().map { it to audioFx.getPresetReverbName(it) }
        _presetReverbIndex.value = audioFx.getCurrentPresetReverb()

        loadPresets()
    }

    fun onPresetSaved(preset: CustomPreset) {
        audioFx.useCustomPreset(preset)
        loadPresets()
    }

    fun onDeletePresetClicked(preset: CustomPreset) {
        repository.delete(preset)
                .doOnComplete {
                    audioFx.unusePreset()
                    loadPresets()
                }
                .subscribeFor(schedulerProvider) {
                }
    }

    fun onEnableStatusChanged(enabled: Boolean) {
        audioFx.setEnabled(enabled)
        audioFx.save()
    }

    fun onPresetSelected(preset: Preset) {
        when (preset) {
            is NativePreset -> audioFx.useNativePreset(preset)
            is CustomPreset -> audioFx.useCustomPreset(preset)
            is VoidPreset -> audioFx.unusePreset()
            else -> Unit
        }
        audioFx.save()
    }

    fun onPresetReverbSelected(index: Short) {
        audioFx.usePresetReverb(index)
        audioFx.save()
    }

    fun onBandLevelChanged(band: Short, level: Short) {
        bandLevelPublisher.onNext(Pair(band, level))
        audioFx.save()
    }

    fun onBassStrengthChanged(strength: Short) {
        bassBoostPublisher.onNext(strength)
        audioFx.save()
    }

    fun onVirtStrengthChanged(strength: Short) {
        virtualizerPublisher.onNext(strength)
        audioFx.save()
    }

    fun onPlaybackParamsOptionSelected() {
        navigator.openPlaybackParams()
    }

    fun onSavePresetButtonClicked(currentBandLevels: ShortArray) {
        navigator.savePreset(currentBandLevels)
    }

    override fun onCleared() {
        super.onCleared()
        audioFx.unregisterObserver(audioFxObserver)
        audioFx.save()
    }

}