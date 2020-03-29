package com.frolo.muse.ui.main.audiofx

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.engine.AudioFx
import com.frolo.muse.engine.AudioFxObserver
import com.frolo.muse.engine.Player
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.ShortRange
import com.frolo.muse.model.preset.CustomPreset
import com.frolo.muse.model.preset.Preset
import com.frolo.muse.model.preset.VoidPreset
import com.frolo.muse.model.reverb.Reverb
import com.frolo.muse.repository.PresetRepository
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseViewModel
import io.reactivex.processors.PublishProcessor
import java.util.concurrent.TimeUnit
import javax.inject.Inject


// TODO: check on which schedulers are all rx sources subscribed in here
class AudioFxViewModel @Inject constructor(
    private val player: Player,
    private val audioFx: AudioFx,
    private val schedulerProvider: SchedulerProvider,
    private val repository: PresetRepository,
    private val navigator: Navigator,
    private val eventLogger: EventLogger
): BaseViewModel(eventLogger) {

    private val voidPreset = repository.voidPreset.blockingGet()

    private val bassBoostPublisher: PublishProcessor<Short> by lazy {
        PublishProcessor.create<Short>().also { publisher ->
            publisher
                .debounce(200, TimeUnit.MILLISECONDS)
                .onBackpressureLatest()
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
                .debounce(200, TimeUnit.MILLISECONDS)
                .onBackpressureLatest()
                .subscribeOn(schedulerProvider.worker())
                .observeOn(schedulerProvider.main())
                .subscribeFor { value ->
                    audioFx.virtualizerStrength = value
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
        override fun onReverbUsed(audioFx: AudioFx, reverb: Reverb) {
            _selectedReverb.value = reverb
        }
    }

    //region LiveData members
    private val _audioSessionId = MutableLiveData<Int>()
    val audioSessionId: LiveData<Int> get() = _audioSessionId

    // Available status
    private val _audioFxAvailable = MutableLiveData<Boolean>()
    val audioFxAvailable: LiveData<Boolean> get() = _audioFxAvailable

    private val _equalizerAvailable = MutableLiveData<Boolean>()
    val equalizerAvailable: LiveData<Boolean> get() = _equalizerAvailable

    private val _bassBoostAvailable = MutableLiveData<Boolean>()
    val bassBoostAvailable: LiveData<Boolean> get() = _bassBoostAvailable

    private val _virtualizerAvailable = MutableLiveData<Boolean>()
    val virtualizerAvailable: LiveData<Boolean> get() = _virtualizerAvailable

    private val _presetReverbAvailable = MutableLiveData<Boolean>()
    val presetReverbAvailable: LiveData<Boolean> get() = _presetReverbAvailable

    private val _audioFxEnabled = MutableLiveData<Boolean>()
    val audioFxEnabled: LiveData<Boolean> get() = _audioFxEnabled

    private val _bandLevels = MutableLiveData<AudioFx>()
    val bandLevels: LiveData<AudioFx> get() = _bandLevels

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
    //endregion

    init {
        audioFx.registerObserver(audioFxObserver)
        onOpened()
    }

    private fun loadPresets() {
        repository.presets
            .subscribeOn(schedulerProvider.worker())
            .observeOn(schedulerProvider.computation())
            .map { customPresets ->
                val nativePresets = audioFx.nativePresets
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
        _presetReverbAvailable.value = audioFx.hasPresetReverbEffect()
        // enabled status
        _audioFxEnabled.value = audioFx.isEnabled
        // equalizer
        _bandLevels.value = audioFx
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

    fun onPresetSaved(preset: CustomPreset) {
        audioFx.usePreset(preset)
        loadPresets()
    }

    fun onDeletePresetClicked(preset: CustomPreset) {
        repository.delete(preset)
            .subscribeOn(schedulerProvider.worker())
            .observeOn(schedulerProvider.main())
            .subscribeFor(schedulerProvider) {
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

    override fun onCleared() {
        super.onCleared()
        audioFx.unregisterObserver(audioFxObserver)
        audioFx.save()
    }

}