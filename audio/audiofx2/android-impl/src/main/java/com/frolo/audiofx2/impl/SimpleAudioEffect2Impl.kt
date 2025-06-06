package com.frolo.audiofx2.impl

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.GuardedBy
import com.frolo.audiofx2.*


internal class BassBoostImpl(
    private val context: Context,
    private val storageKey: String,
    private val errorHandler: AudioEffect2ErrorHandler
): SimpleAudioEffect2Impl<android.media.audiofx.BassBoost>(
    context = context,
    storageKey = storageKey,
    errorHandler = errorHandler
), BassBoost {
    override val effectKey: String get() = "bass_boost"

    override fun getStrengthFrom(effect: android.media.audiofx.BassBoost): Int {
        return effect.roundedStrength.toInt()
    }

    override fun setStrengthTo(effect: android.media.audiofx.BassBoost, value: Int) {
        effect.setStrength(value.toShort())
    }

    override val descriptor: AudioEffectDescriptor by lazy {
        SimpleAudioEffectDescriptor(name = context.getString(R.string.bass_boost))
    }
    override val valueRange: ValueRange = ValueRange(minValue = 0, maxValue = 1000)

    override fun instantiateEngine(priority: Int, audioSessionId: Int): android.media.audiofx.BassBoost {
        return android.media.audiofx.BassBoost(priority, audioSessionId)
    }
}

internal class VirtualizerImpl(
    private val context: Context,
    private val storageKey: String,
    private val errorHandler: AudioEffect2ErrorHandler
): SimpleAudioEffect2Impl<android.media.audiofx.Virtualizer>(
    context = context,
    storageKey = storageKey,
    errorHandler = errorHandler
), Virtualizer {
    override val effectKey: String get() = "virtualizer"

    override fun getStrengthFrom(effect: android.media.audiofx.Virtualizer): Int {
        return effect.roundedStrength.toInt()
    }

    override fun setStrengthTo(effect: android.media.audiofx.Virtualizer, value: Int) {
        effect.setStrength(value.toShort())
    }

    override val descriptor: AudioEffectDescriptor by lazy {
        SimpleAudioEffectDescriptor(name = context.getString(R.string.virtualizer))
    }
    override val valueRange: ValueRange = ValueRange(minValue = 0, maxValue = 1000)

    override fun instantiateEngine(priority: Int, audioSessionId: Int): android.media.audiofx.Virtualizer {
        return android.media.audiofx.Virtualizer(priority, audioSessionId)
    }
}

internal abstract class SimpleAudioEffect2Impl<E: android.media.audiofx.AudioEffect>(
    private val context: Context,
    private val storageKey: String,
    private val errorHandler: AudioEffect2ErrorHandler
): BaseAudioEffect2Impl<E>(), SimpleAudioEffect2 {

    private val lock = Any()
    @GuardedBy("lock")
    private var engine: E? = null
    @get:GuardedBy("lock")
    private val state by lazy { SimpleAudioEffect2State(context, storageKey, effectKey) }

    final override var value: Int
        get() = synchronized(lock) {
            engine
                ?.runCatching { getStrengthFrom(this) }
                ?.onFailure { errorHandler.onAudioEffectError(this, it) }
                ?.getOrNull() ?: state.getValue()
        }
        set(value) = synchronized(lock) {
            state.setValue(value)
            engine
                ?.runCatching { setStrengthTo(this, value) }
                ?.onFailure { errorHandler.onAudioEffectError(this, it) }
            valueChangeListenerRegistry.dispatchEffectValueChange(value)
        }

    final override var isEnabled: Boolean
        get() = synchronized(lock) {
            engine
                ?.runCatching { this.enabled }
                ?.onFailure { errorHandler.onAudioEffectError(this, it) }
                ?.getOrNull() ?: state.isEnabled()
        }
        set(value) = synchronized(lock) {
            state.setEnabled(value)
            engine
                ?.runCatching { setEnabledOrThrow(value) }
                ?.onFailure { errorHandler.onAudioEffectError(this, it) }
            enableStatusChangeListenerRegistry.dispatchEnableStatusChange(value)
        }

    abstract val effectKey: String

    private val enableStatusChangeListenerRegistry =
        EnableStatusChangeListenerRegistry(context, this)
    private val valueChangeListenerRegistry = ValueChangeListenerRegistry(context, this)

    final override fun addOnEnableStatusChangeListener(listener: AudioEffect2.OnEnableStatusChangeListener) {
        enableStatusChangeListenerRegistry.addListener(listener)
    }

    final override fun removeOnEnableStatusChangeListener(listener: AudioEffect2.OnEnableStatusChangeListener) {
        enableStatusChangeListenerRegistry.removeListener(listener)
    }

    final override fun addOnEffectValueChangeListener(listener: SimpleAudioEffect2.OnEffectValueChangeListener) {
        valueChangeListenerRegistry.addListener(listener)
    }

    final override fun removeOnEffectValueChangeListener(listener: SimpleAudioEffect2.OnEffectValueChangeListener) {
        valueChangeListenerRegistry.removeListener(listener)
    }

    final override fun onAttachTo(target: AudioFx2AttachTarget) = synchronized(lock) {
        try {
            engine?.release()
            engine = null
        } catch (e: Throwable) {
            errorHandler.onAudioEffectError(this, e)
        }
        try {
            val newEngine = instantiateEngine(target.priority, target.sessionId)
            newEngine.enabled = isEnabled
            setStrengthTo(newEngine, state.getValue())
            this.engine = newEngine
        } catch (e: Throwable) {
            errorHandler.onAudioEffectError(this, e)
        }
    }

    final override fun onRelease() = synchronized(lock) {
        try {
            engine?.release()
            engine = null
        } catch (e: Throwable) {
            errorHandler.onAudioEffectError(this, e)
        }
    }

    abstract fun getStrengthFrom(effect: E): Int
    abstract fun setStrengthTo(effect: E, value: Int)
    abstract fun instantiateEngine(priority: Int, audioSessionId: Int): E
}

private class SimpleAudioEffect2State(
    private val context: Context,
    private val storageKey: String,
    private val effectKey: String
) {
    private val lock = Any()
    @get:GuardedBy("lock")
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("$storageKey.audiofx2.$effectKey", Context.MODE_PRIVATE)
    }

    fun isEnabled(): Boolean = synchronized(lock) {
        prefs.getBoolean(KEY_ENABLED, false)
    }

    fun setEnabled(enabled: Boolean) = synchronized(lock) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun getValue(): Int = synchronized(lock) {
        prefs.getInt(KEY_VALUE, 0)
    }

    fun setValue(value: Int) = synchronized(lock) {
        prefs.edit().putInt(KEY_VALUE, value).apply()
    }

    companion object {
        private const val KEY_ENABLED = "enabled"
        private const val KEY_VALUE = "value"
    }
}

private class ValueChangeListenerRegistry(
    context: Context,
    private val effect: SimpleAudioEffect2
): ListenerRegistry<SimpleAudioEffect2.OnEffectValueChangeListener>(context){
    fun dispatchEffectValueChange(value: Int) = doDispatch { listener ->
        listener.onEffectValueChange(effect, value)
    }
}