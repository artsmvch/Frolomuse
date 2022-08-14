package com.frolo.audiofx2.impl

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.GuardedBy
import com.frolo.audiofx2.*

internal class EqualizerImpl(
    private val context: Context,
    private val storageKey: String,
    private val errorHandler: AudioEffect2ErrorHandler
): BaseAudioEffect2Impl<android.media.audiofx.Equalizer>(), Equalizer, EqualizerPresetStorage {
    private val defaults = Defaults(context)
    private val state = EqualizerState(context, storageKey, defaults)
    private val lock = Any()
    @GuardedBy("lock")
    private var engine: android.media.audiofx.Equalizer? = null

    override val descriptor: AudioEffectDescriptor =
        SimpleAudioEffectDescriptor(name = "Equalizer")

    override var isEnabled: Boolean
        get() {
            return runOnEngine(
                action = { it.enabled },
                fallback = { state.isEnabled() }
            )
        }
        set(value) {
            state.setEnabled(value)
            runOnEngine(
                action = { it.enabled = value },
                fallback = { }
            )
        }

    private val enableStatusChangeListenerRegistry =
        EnableStatusChangeListenerRegistry(context, this)
    private val bandLevelChangeListenerRegistry =
        BandLevelChangeListenerRegistry(context, this)

    override val numberOfBands: Int get() {
        return runOnEngine(
            action = { it.numberOfBands.toInt() },
            fallback = { defaults.numberOfBands }
        )
    }

    override val bandLevelRange: EffectValueRange get() {
        return runOnEngine(
            action = {
                val arr = it.bandLevelRange
                EffectValueRange(
                    minLevel = arr[0].toInt(),
                    maxLevel = arr[1].toInt()
                )
            },
            fallback = {
                EffectValueRange(
                    minLevel = defaults.minBandLevelRange,
                    maxLevel = defaults.maxBandLevelRange
                )
            }
        )
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

    override fun getBandLevel(bandIndex: Int): Int {
        return runOnEngine(
            action = { it.getBandLevel(bandIndex.toShort()).toInt() },
            fallback = { state.getBandLevel(bandIndex) }
        )
    }

    override fun setBandLevel(bandIndex: Int, level: Int) = synchronized(lock) {
        state.setBandLevel(bandIndex, level)
        equalizerPresetStorageImpl.unusePreset()
        return@synchronized runOnEngine(
            action = { it.setBandLevel(bandIndex.toShort(), level.toShort()) },
            fallback = { }
        )
    }

    override fun getFreqRange(bandIndex: Int): EffectValueRange {
        return runOnEngine(
            action = {
                val arr = it.getBandFreqRange(bandIndex.toShort())
                EffectValueRange(
                    minLevel = arr[0],
                    maxLevel = arr[1]
                )
            },
            fallback = { defaults.getDefaultBandFreqRange(bandIndex) }
        )
    }

    private fun <R> runOnEngine(
        action: (engine: android.media.audiofx.Equalizer) -> R,
        fallback: () -> R
    ): R {
        return synchronized(lock) {
            try {
                engine?.let {
                    action.invoke(it)
                }
            } catch (e: Throwable) {
                errorHandler.onAudioEffectError(this, e)
            }
            return@synchronized fallback.invoke()
        }
    }

    override fun onApplyToAudioSession(priority: Int, audioSessionId: Int) = synchronized(lock) {
        try {
            engine?.release()
            engine = null
        } catch (e: Throwable) {
            errorHandler.onAudioEffectError(this, e)
        }
        val newEngine = android.media.audiofx.Equalizer(priority, audioSessionId)
        newEngine.enabled = isEnabled
        repeat(state.getNumberOfBands()) { iteration ->
            newEngine.setBandLevel(iteration.toShort(), state.getBandLevel(iteration).toShort())
        }
        newEngine.setEnableStatusListener { _, enabled ->
            enableStatusChangeListenerRegistry.dispatchEnableStatusChange(enabled)
        }
        newEngine.setParameterListener { _, _, param1, param2, value ->
            if (param1 == android.media.audiofx.Equalizer.PARAM_BAND_LEVEL) {
                bandLevelChangeListenerRegistry.dispatchBandLevelChange(
                    band = param2, level = value)
            }
        }
        this.engine = newEngine
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

    override fun usePreset(preset: EqualizerPreset) = synchronized(lock) {
        when (preset) {
            is NativePresetImpl -> {
                runOnEngine(
                    action = {
                        it.usePreset(preset.index.toShort())
                    },
                    fallback = { }
                )
            }
            is CustomPresetImpl -> {
                runOnEngine(
                    action = {
                        for (band in 0 until preset.numberOfBands) {
                            it.setBandLevel(band.toShort(), preset.getBandLevel(band).toShort())
                        }
                    },
                    fallback = { }
                )
            }
            else -> {
            }
        }
        equalizerPresetStorageImpl.usePreset(preset)
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