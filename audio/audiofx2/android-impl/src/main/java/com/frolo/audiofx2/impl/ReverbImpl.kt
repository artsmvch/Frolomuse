package com.frolo.audiofx2.impl

import android.content.Context
import android.content.SharedPreferences
import android.media.audiofx.PresetReverb
import androidx.annotation.GuardedBy
import androidx.annotation.StringRes
import com.frolo.audiofx2.AudioEffect2
import com.frolo.audiofx2.AudioEffectDescriptor
import com.frolo.audiofx2.Reverb


internal class ReverbImpl constructor(
    private val context: Context,
    private val storageKey: String,
    private val errorHandler: AudioEffect2ErrorHandler,
    private val initialEffectParams: EffectInitParams
): BaseAudioEffect2Impl<android.media.audiofx.PresetReverb>(), Reverb {
    private val lock = Any()
    @GuardedBy("lock")
    private var engine: android.media.audiofx.PresetReverb? = null
    private val state by lazy {
        ReverbState(
            context = context,
            storageKey = storageKey,
            presetFactory = ::mapPresetFromValue
        )
    }

    private val enableStatusChangeListenerRegistry =
        EnableStatusChangeListenerRegistry(context, this)
    private val presetUsedListenerRegistry =
        ReverbPresetUsedListenerRegistry(context, this)

    override val descriptor: AudioEffectDescriptor by lazy {
        SimpleAudioEffectDescriptor(
            name = context.getString(R.string.reverb)
        )
    }
    override var isEnabled: Boolean
        get() = synchronized(lock) {
            engine
                ?.runCatching { this.enabled }
                ?.onFailure { errorHandler.onAudioEffectError(this, it) }
                ?.getOrNull() ?: state.isEnabled()
        }
        set(value) = synchronized(lock) {
            state.setEnabled(value)
            engine
                ?.runCatching { this.enabled = value }
                ?.onFailure { errorHandler.onAudioEffectError(this, it) }
        }

    private val availablePresetsImpl: List<ReverbPresetImpl> by lazy {
        val values = listOf(
            PresetReverb.PRESET_NONE,
            PresetReverb.PRESET_LARGEHALL,
            PresetReverb.PRESET_LARGEROOM,
            PresetReverb.PRESET_MEDIUMHALL,
            PresetReverb.PRESET_MEDIUMROOM,
            PresetReverb.PRESET_PLATE,
            PresetReverb.PRESET_SMALLROOM
        )
        return@lazy values
            .map(::mapPresetFromValue)
            .sortedBy { it.level }
    }
    override val availablePresets: List<Reverb.Preset> get() = availablePresetsImpl

    override var preset: Reverb.Preset
        get() = synchronized(lock) {
            engine
                ?.runCatching { this.preset.let(::mapPresetFromValue) }
                ?.onFailure { errorHandler.onAudioEffectError(this, it) }
                ?.getOrNull() ?: state.getCurrentPreset()
        }
        set(value) = synchronized(lock) {
            state.setCurrentPreset(value)
            engine
                ?.runCatching { engine?.preset = (value as ReverbPresetImpl).value }
                ?.onFailure { errorHandler.onAudioEffectError(this, it) }
            presetUsedListenerRegistry.dispatchPresetUsed(value)
        }

    init {
        applyToAudioSession(initialEffectParams.audioSessionId)
    }

    private fun mapPresetFromValue(value: Short): ReverbPresetImpl {
        val level: Int
        @StringRes
        val nameResId: Int;
        when(value) {
            PresetReverb.PRESET_LARGEHALL -> {
                level = 1
                nameResId = R.string.preset_reverb_large_hall
            }
            PresetReverb.PRESET_LARGEROOM -> {
                level = 2
                nameResId = R.string.preset_reverb_large_room
            }
            PresetReverb.PRESET_MEDIUMHALL -> {
                level = 3
                nameResId = R.string.preset_reverb_medium_hall
            }
            PresetReverb.PRESET_MEDIUMROOM -> {
                level = 4
                nameResId = R.string.preset_reverb_medium_room
            }
            PresetReverb.PRESET_PLATE -> {
                level = 5
                nameResId = R.string.preset_reverb_plate
            }
            PresetReverb.PRESET_SMALLROOM -> {
                level = 6
                nameResId = R.string.preset_reverb_small_room
            }
            else -> {
                level = 0
                nameResId = R.string.preset_reverb_none
            }
        }
        return ReverbPresetImpl(
            value = value,
            level = level,
            name = context.getString(nameResId)
        )
    }

    override fun onApplyToAudioSession(priority: Int, audioSessionId: Int) = synchronized(lock) {
        try {
            engine?.release()
            engine = null
        } catch (e: Throwable) {
            errorHandler.onAudioEffectError(this, e)
        }
        try {
            val newEngine = PresetReverb(priority, audioSessionId)
            newEngine.enabled = state.isEnabled()
            newEngine.preset = (state.getCurrentPreset() as? ReverbPresetImpl)?.value
                ?: PresetReverb.PRESET_NONE
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

    override fun addOnPresetUsedListener(listener: Reverb.OnPresetUsedListener) {
        presetUsedListenerRegistry.addListener(listener)
    }

    override fun removeOnPresetUsedListener(listener: Reverb.OnPresetUsedListener) {
        presetUsedListenerRegistry.removeListener(listener)
    }
}

private data class ReverbPresetImpl(
    val value: Short,
    override val level: Int,
    override val name: String
) : Reverb.Preset {
    override fun isTheSame(other: Reverb.Preset): Boolean {
        return other is ReverbPresetImpl && this.value == other.value
    }
}

private class ReverbPresetUsedListenerRegistry(
    context: Context,
    private val effect: Reverb
): ListenerRegistry<Reverb.OnPresetUsedListener>(context){
    fun dispatchPresetUsed(preset: Reverb.Preset) = doDispatch { listener ->
        listener.onPresetUsed(effect, preset)
    }
}

private class ReverbState constructor(
    val context: Context,
    val storageKey: String,
    val presetFactory: (Short) -> Reverb.Preset
) {
    private val lock = Any()
    @get:GuardedBy("lock")
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(getPrefsName(storageKey), Context.MODE_PRIVATE)
    }

    fun isEnabled(): Boolean = synchronized(lock) {
        return prefs.getBoolean(KEY_ENABLED, false)
    }

    fun setEnabled(enabled: Boolean) = synchronized(lock) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun getCurrentPreset(): Reverb.Preset = synchronized(lock) {
        val value = prefs.getInt(KEY_PRESET_VALUE, PresetReverb.PRESET_NONE.toInt())
        presetFactory.invoke(value.toShort())
    }

    fun setCurrentPreset(preset: Reverb.Preset) = synchronized(lock) {
        if (preset is ReverbPresetImpl) {
            prefs.edit().putInt(KEY_PRESET_VALUE, preset.value.toInt()).apply()
        }
    }

    companion object {
        private const val KEY_ENABLED = "enabled"
        private const val KEY_PRESET_VALUE = "preset_value"

        private fun getPrefsName(storageKey: String): String {
            return "$storageKey.audiofx2.reverb"
        }
    }
}