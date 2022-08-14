package com.frolo.audiofx2.impl

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import androidx.annotation.GuardedBy
import com.frolo.audiofx2.AudioEffect2
import com.frolo.audiofx2.AudioEffectDescriptor
import com.frolo.audiofx2.EffectValueRange
import com.frolo.audiofx2.Equalizer
import java.lang.NumberFormatException
import java.util.*
import kotlin.collections.HashMap

internal class EqualizerImpl(
    private val context: Context,
    private val storageKey: String,
    private val errorHandler: AudioEffect2ErrorHandler
): BaseAudioEffect2Impl<android.media.audiofx.Equalizer>(), Equalizer {
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

    override var onEnableStatusChangeListener: AudioEffect2.OnEnableStatusChangeListener? = null
    private val onBandLevelChangeListeners = Collections.synchronizedList(
        ArrayList<Equalizer.OnBandLevelChangeListener>())
    private val bandLevelListenerHandler = Handler(context.mainLooper)

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

    override fun getBandLevel(bandIndex: Int): Int {
        return runOnEngine(
            action = { it.getBandLevel(bandIndex.toShort()).toInt() },
            fallback = { state.getBandLevel(bandIndex) }
        )
    }

    override fun setBandLevel(bandIndex: Int, level: Int) {
        state.setBandLevel(bandIndex, level)
        return runOnEngine(
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

    override fun onApplyToAudioSession(priority: Int, audioSessionId: Int) {
        synchronized(lock) {
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
                this.onEnableStatusChangeListener?.onEnableStatusChange(this, enabled)
            }
            newEngine.setParameterListener { _, _, param1, param2, value ->
                if (param1 == android.media.audiofx.Equalizer.PARAM_BAND_LEVEL) {
                    dispatchBandLevelChange(band = param2, level = value)
                }
            }
            this.engine = newEngine
        }
    }

    override fun addOnBandLevelChangeListener(listener: Equalizer.OnBandLevelChangeListener) {
        onBandLevelChangeListeners.add(listener)
    }

    override fun removeOnBandLevelChangeListener(listener: Equalizer.OnBandLevelChangeListener) {
        onBandLevelChangeListeners.remove(listener)
    }

    private fun dispatchBandLevelChange(band: Int, level: Int) {
        bandLevelListenerHandler.post {
            synchronized(onBandLevelChangeListeners) {
                onBandLevelChangeListeners.forEach { listener ->
                    listener.onBandLevelChange(
                        equalizer = this,
                        band = band,
                        level = level
                    )
                }
            }
        }
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