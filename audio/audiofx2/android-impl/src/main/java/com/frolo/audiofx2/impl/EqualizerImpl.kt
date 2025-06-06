package com.frolo.audiofx2.impl

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.GuardedBy
import com.frolo.audiofx2.*
import kotlin.math.max

internal class EqualizerImpl(
    private val context: Context,
    private val storageKey: String,
    private val errorHandler: AudioEffect2ErrorHandler,
    private val initialAttachTarget: AudioFx2AttachTarget
): BaseAudioEffect2Impl<android.media.audiofx.Equalizer>(), Equalizer, EqualizerPresetStorage {
    private val defaults = Defaults(context)
    private val state = EqualizerState(context, storageKey, defaults)
    private val lock = Any()
    @GuardedBy("lock")
    private var engine: android.media.audiofx.Equalizer? = null

    override val descriptor: AudioEffectDescriptor =
        SimpleAudioEffectDescriptor(name = context.getString(R.string.equalizer))

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
                ?.runCatching { setEnabledOrThrow(value) }
                ?.onFailure { errorHandler.onAudioEffectError(this, it) }
            enableStatusChangeListenerRegistry.dispatchEnableStatusChange(value)
        }

    private val enableStatusChangeListenerRegistry =
        EnableStatusChangeListenerRegistry(context, this)
    private val bandLevelChangeListenerRegistry =
        BandLevelChangeListenerRegistry(context, this)
    private val presetUsedListenerRegistry =
        PresetUsedListenerRegistry(context, this)

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
        attachTo(initialAttachTarget)
    }

    override fun getBandLevelsSnapshot(): Map<Int, Int> = synchronized(lock) {
        return@synchronized engine
            ?.runCatching {
                val numberOfBands = this.numberOfBands.toInt()
                val levels = HashMap<Int, Int>(numberOfBands)
                for (band in 0 until numberOfBands) {
                    levels[band] = this.getBandLevel(band.toShort()).toInt()
                }
                return@runCatching levels
            }?.getOrNull()
            // If null or error, retrieve snapshot from the storage
            ?: equalizerPresetStorageImpl.run {
                val numberOfBands = this.getNumberOfBands()
                val levels = HashMap<Int, Int>(numberOfBands)
                for (band in 0 until numberOfBands) {
                    levels[band] = this.getBandLevel(band)
                }
                return@run levels
            }
    }

    override fun getBandLevel(bandIndex: Int): Int = synchronized(lock) {
        engine
            ?.runCatching { this.getBandLevel(bandIndex.toShort()) }
            ?.onFailure { errorHandler.onAudioEffectError(this, it) }
            ?.getOrNull()?.toInt() ?: equalizerPresetStorageImpl.getBandLevel(bandIndex)
    }

    override fun setBandLevel(bandIndex: Int, level: Int) = synchronized(lock) {
        val usedPreset = equalizerPresetStorageImpl.setBandLevel(bandIndex, level)
        engine
            ?.runCatching { this.setBandLevel(bandIndex.toShort(), level.toShort()) }
            ?.onFailure { errorHandler.onAudioEffectError(this, it) }
        bandLevelChangeListenerRegistry.dispatchBandLevelChange(bandIndex, level)
        presetUsedListenerRegistry.dispatchPresetUsed(usedPreset)
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

    override fun onAttachTo(target: AudioFx2AttachTarget) = synchronized(lock) {
        try {
            engine?.release()
            engine = null
        } catch (e: Throwable) {
            errorHandler.onAudioEffectError(this, e)
        }
        try {
            val newEngine = android.media.audiofx.Equalizer(target.priority, target.sessionId)
            newEngine.enabled = isEnabled
            val preset = getCurrentPreset()
            if (preset != null) {
                usePresetActual(newEngine, preset)
            } else {
                for (band in 0 until equalizerPresetStorageImpl.getNumberOfBands()) {
                    newEngine.setBandLevel(band.toShort(),
                        equalizerPresetStorageImpl.getBandLevel(band).toShort())
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
        // FIXME: Attach to fake audio session. We still need an instance of the engine.
        attachTo(initialAttachTarget)
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

    override fun addOnPresetUsedListener(listener: Equalizer.OnPresetUsedListener) {
        presetUsedListenerRegistry.addListener(listener)
    }

    override fun removeOnPresetUsedListener(listener: Equalizer.OnPresetUsedListener) {
        presetUsedListenerRegistry.removeListener(listener)
    }

    override fun getCurrentPreset(): EqualizerPreset {
        return equalizerPresetStorageImpl.getCurrentPreset()
    }

    override fun usePreset(preset: EqualizerPreset): Unit = synchronized(lock) {
        usePresetActual(engine, preset)
    }

    private fun usePresetActual(engine: android.media.audiofx.Equalizer?, preset: EqualizerPreset): Unit = synchronized(lock) {
        val syncBandLevels = HashMap<Int, Int>(5, 1f)
        when (preset) {
            is CustomPresetImpl -> {
                engine
                    ?.runCatching {
                        for (band in 0 until max(this.numberOfBands.toInt(),
                            equalizerPresetStorageImpl.getNumberOfBands())) {
                            val level = equalizerPresetStorageImpl.getBandLevel(band)
                            this.setBandLevel(band.toShort(), level.toShort())
                            syncBandLevels[band] = level
                        }
                    }
                    ?.onFailure { errorHandler.onAudioEffectError(this, it) }
            }
            is NativePresetImpl -> {
                engine
                    ?.runCatching { this.usePreset(preset.index.toShort()) }
                    ?.onFailure { errorHandler.onAudioEffectError(this, it) }
                // Sync all band levels after applying a native preset
                engine?.runCatching {
                    for (band in 0 until this.numberOfBands) {
                        syncBandLevels[band] = this.getBandLevel(band.toShort()).toInt()
                    }
                }
            }
            is SavedPresetImpl -> {
                engine
                    ?.runCatching {
                        for (band in 0 until preset.numberOfBands) {
                            this.setBandLevel(band.toShort(), preset.getBandLevel(band).toShort())
                        }
                    }
                    ?.onFailure { errorHandler.onAudioEffectError(this, it) }
                // Sync all band levels after applying a saved preset
                for (band in 0 until preset.numberOfBands) {
                    syncBandLevels[band] = preset.getBandLevel(band)
                }
            }
            else -> Unit
        }
        equalizerPresetStorageImpl.setPreset(preset, syncBandLevels)
        presetUsedListenerRegistry.dispatchPresetUsed(preset)
        syncBandLevels.entries.forEach { entry ->
            bandLevelChangeListenerRegistry.dispatchBandLevelChange(
                band = entry.key,
                level = entry.value
            )
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

    override fun addOnStorageUpdateListener(listener: EqualizerPresetStorage.OnStorageUpdateListener) {
        equalizerPresetStorageImpl.addOnStorageUpdateListener(listener)
    }

    override fun removeOnStorageUpdateListener(listener: EqualizerPresetStorage.OnStorageUpdateListener) {
        equalizerPresetStorageImpl.removeOnStorageUpdateListener(listener)
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
        context.getSharedPreferences("$storageKey.audiofx2.equalizer", Context.MODE_PRIVATE)
    @GuardedBy("lock")
    private var isEnabled: Boolean = false

    init {
        restoreState()
    }

    private fun restoreState() = synchronized(lock) {
        isEnabled = prefs.getBoolean(KEY_ENABLED, false)
    }

    fun isEnabled(): Boolean = synchronized(lock) { isEnabled }

    fun setEnabled(enabled: Boolean) = synchronized(lock) {
        isEnabled = enabled
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    companion object {
        private const val KEY_ENABLED = "enabled"
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

private class PresetUsedListenerRegistry(
    context: Context,
    private val effect: Equalizer
): ListenerRegistry<Equalizer.OnPresetUsedListener>(context){
    fun dispatchPresetUsed(preset: EqualizerPreset) = doDispatch { listener ->
        listener.onPresetUsed(effect, preset)
    }
}