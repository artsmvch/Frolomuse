package com.frolo.audiofx2.impl

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.GuardedBy
import com.frolo.audiofx2.*

internal class EqualizerImpl(
    private val context: Context,
    private val storageKey: String,
    private val errorHandler: AudioEffect2ErrorHandler,
    private val initialEffectParams: EffectInitParams
): BaseAudioEffect2Impl<android.media.audiofx.Equalizer>(), Equalizer, EqualizerPresetStorage {
    private val defaults = Defaults(context)
    private val state = EqualizerState(context, storageKey, defaults)
    private val lock = Any()
    @GuardedBy("lock")
    private var engine: android.media.audiofx.Equalizer? = null

    override val descriptor: AudioEffectDescriptor =
        SimpleAudioEffectDescriptor(name = "Equalizer")

    override var isEnabled: Boolean
        get() = synchronized(lock) {
            engine
                ?.runCatching { this.enabled }
                ?.onFailure { errorHandler.onAudioEffectError(this, it) }
                ?.getOrNull()?: state.isEnabled()
        }
        set(value) = synchronized(lock) {
            state.setEnabled(value)
            engine
                ?.runCatching { this.enabled = value }
                ?.onFailure { errorHandler.onAudioEffectError(this, it) }
            enableStatusChangeListenerRegistry.dispatchEnableStatusChange(value)
        }

    private val enableStatusChangeListenerRegistry =
        EnableStatusChangeListenerRegistry(context, this)
    private val bandLevelChangeListenerRegistry =
        BandLevelChangeListenerRegistry(context, this)

    override val numberOfBands: Int get() = synchronized(lock) {
        engine
            ?.runCatching { this.numberOfBands }
            ?.onFailure { errorHandler.onAudioEffectError(this, it) }
            ?.getOrNull()?.toInt() ?: defaults.numberOfBands
    }

    override val bandLevelRange: ValueRange get() = synchronized(lock) {
        engine
            ?.runCatching {
                val arr = this.bandLevelRange
                ValueRange(
                    minValue = arr[0].toInt(),
                    maxValue = arr[1].toInt()
                )
            }
            ?.onFailure { errorHandler.onAudioEffectError(this, it) }
            ?.getOrNull() ?: kotlin.run {
                ValueRange(
                    minValue = defaults.minBandLevelRange,
                    maxValue = defaults.maxBandLevelRange
                )
            }
    }

    private val equalizerPresetStorageImpl = EqualizerPresetStorageImpl(
        context = context,
        databaseHelper = AudioEffect2DatabaseHelper(
            context = context,
            storageKey = storageKey
        ),
        storageKey = storageKey,
        defaults = defaults
    )

    init {
        onApplyToAudioSession(initialEffectParams.priority, initialEffectParams.audioSessionId)
    }

    override fun getBandLevel(bandIndex: Int): Int = synchronized(lock) {
        engine
            ?.runCatching { this.getBandLevel(bandIndex.toShort()) }
            ?.onFailure { errorHandler.onAudioEffectError(this, it) }
            ?.getOrNull()?.toInt() ?: state.getBandLevel(bandIndex)
    }

    override fun setBandLevel(bandIndex: Int, level: Int) = synchronized(lock) {
        state.setBandLevel(bandIndex, level)
        equalizerPresetStorageImpl.unusePreset()
        kotlin.runCatching { engine?.setBandLevel(bandIndex.toShort(), level.toShort()) }
        bandLevelChangeListenerRegistry.dispatchBandLevelChange(bandIndex, level)
    }

    override fun getFreqRange(bandIndex: Int): ValueRange = synchronized(lock) {
        engine?.runCatching {
            val arr = this.getBandFreqRange(bandIndex.toShort())
            ValueRange(
                minValue = arr[0],
                maxValue = arr[1]
            ) }
            ?.onFailure { errorHandler.onAudioEffectError(this, it) }
            ?.getOrNull() ?: defaults.getDefaultBandFreqRange(bandIndex)
    }

    override fun onApplyToAudioSession(priority: Int, audioSessionId: Int) = synchronized(lock) {
        try {
            engine?.release()
            engine = null
        } catch (e: Throwable) {
            errorHandler.onAudioEffectError(this, e)
        }
        try {
            val newEngine = android.media.audiofx.Equalizer(priority, audioSessionId)
            newEngine.enabled = isEnabled
            val preset = getCurrentPreset()
            if (preset != null) {
                usePreset(preset)
            } else {
                for (band in 0 until state.getNumberOfBands()) {
                    newEngine.setBandLevel(band.toShort(), state.getBandLevel(band).toShort())
                }
            }
            this.engine = newEngine
        } catch (e: Throwable) {
            errorHandler.onAudioEffectError(this, e)
        }
    }

    override fun onRelease() = synchronized(lock) {
        try {
            engine?.release()
            engine = null
        } catch (e: Throwable) {
            errorHandler.onAudioEffectError(this, e)
        }
    }

    override fun addOnEnableStatusChangeListener(listener: AudioEffect2.OnEnableStatusChangeListener) {
        enableStatusChangeListenerRegistry.addListener(listener)
    }

    override fun removeOnEnableStatusChangeListener(listener: AudioEffect2.OnEnableStatusChangeListener) {
        enableStatusChangeListenerRegistry.removeListener(listener)
    }

    override fun addOnBandLevelChangeListener(listener: Equalizer.OnBandLevelChangeListener) {
        bandLevelChangeListenerRegistry.addListener(listener)
    }

    override fun removeOnBandLevelChangeListener(listener: Equalizer.OnBandLevelChangeListener) {
        bandLevelChangeListenerRegistry.removeListener(listener)
    }

    override fun getCurrentPreset(): EqualizerPreset? {
        return equalizerPresetStorageImpl.getCurrentPreset()
    }

    override fun usePreset(preset: EqualizerPreset): Unit = synchronized(lock) {
        equalizerPresetStorageImpl.usePreset(preset)
        when (preset) {
            is NativePresetImpl -> {
                engine.runCatching { this?.usePreset(preset.index.toShort()) }
                    .onFailure { errorHandler.onAudioEffectError(this, it) }
                // Check all bands after applying native preset
                engine?.runCatching {
                    for (band in 0 until this.numberOfBands) {
                        bandLevelChangeListenerRegistry.dispatchBandLevelChange(
                            band = band,
                            level = this.getBandLevel(band.toShort()).toInt()
                        )
                    }
                }
            }
            is CustomPresetImpl -> {
                engine?.runCatching {
                    for (band in 0 until preset.numberOfBands) {
                        this.setBandLevel(band.toShort(), preset.getBandLevel(band).toShort())
                    }
                }?.onFailure { errorHandler.onAudioEffectError(this, it) }
                for (band in 0 until preset.numberOfBands) {
                    bandLevelChangeListenerRegistry.dispatchBandLevelChange(
                        band = band,
                        level = preset.getBandLevel(band)
                    )
                }
            }
            else -> Unit
        }
    }

    override fun getAllPresets(): List<EqualizerPreset> {
        return equalizerPresetStorageImpl.getAllPresets()
    }

    override fun createPreset(name: String, bandLevels: Map<Int, Int>): EqualizerPreset {
        return equalizerPresetStorageImpl.createPreset(name, bandLevels)
    }

    override fun deletePreset(preset: EqualizerPreset) {
        equalizerPresetStorageImpl.deletePreset(preset)
    }
}

private class EqualizerState(
    private val context: Context,
    private val storageKey: String,
    private val defaults: Defaults
) {
    private val lock = Any()
    @GuardedBy("lock")
    private val prefs: SharedPreferences =
        context.getSharedPreferences("$storageKey:audiofx2:equalizer", Context.MODE_PRIVATE)
    @GuardedBy("lock")
    private var isEnabled: Boolean = false
    @GuardedBy("lock")
    private val bandLevels = HashMap<Int, Int>()

    init {
        synchronized(lock) {
            isEnabled = prefs.getBoolean(KEY_ENABLED, false)
            prefs.all?.entries?.forEach { entry ->
                if (entry.key.startsWith(KEY_BAND_LEVEL_PREFIX)) {
                    try {
                        val bandIndex = entry.key.substring(KEY_BAND_LEVEL_PREFIX.length).toInt()
                        val bandLevel = entry.value?.toString()!!.toInt()
                        bandLevels[bandIndex] = bandLevel
                    } catch (ignored: NumberFormatException) {
                    }
                }
            }
        }
    }

    fun isEnabled(): Boolean {
        return synchronized(lock) { isEnabled }
    }

    fun setEnabled(enabled: Boolean) {
        synchronized(lock) {
            isEnabled = enabled
            prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
        }
    }

    fun getNumberOfBands(): Int {
        return synchronized(lock) {
            bandLevels.size
        }
    }

    fun getBandLevel(bandIndex: Int): Int {
        return synchronized(lock) {
            bandLevels[bandIndex] ?: defaults.zeroBandLevel
        }
    }

    fun setBandLevel(bandIndex: Int, bandLevel: Int) {
        synchronized(lock) {
            if (bandLevels.isEmpty()) {
                repeat(defaults.numberOfBands) { iteration ->
                    bandLevels[iteration] = defaults.zeroBandLevel
                }
            }
            bandLevels[bandIndex] = bandLevel
            prefs.edit().putInt(KEY_BAND_LEVEL_PREFIX + bandIndex, bandLevel).apply()
        }
    }

    companion object {
        private const val KEY_ENABLED = "enabled"
        private const val KEY_BAND_LEVEL_PREFIX = "band_level_"
    }
}

private class BandLevelChangeListenerRegistry(
    context: Context,
    private val effect: Equalizer
): ListenerRegistry<Equalizer.OnBandLevelChangeListener>(context){
    fun dispatchBandLevelChange(band: Int, level: Int) = doDispatch { listener ->
        listener.onBandLevelChange(effect, band, level)
    }
}