package com.frolo.muse.engine

import android.content.Context
import android.media.audiofx.AudioEffect
import android.media.audiofx.BassBoost
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import android.os.Handler
import android.os.Looper
import androidx.annotation.MainThread
import com.frolo.muse.BuildConfig
import com.frolo.muse.R
import com.frolo.muse.ThreadStrictMode
import com.frolo.muse.Trace
import com.frolo.muse.model.preset.CustomPreset
import com.frolo.muse.model.preset.NativePreset
import com.frolo.muse.model.preset.Preset
import com.frolo.muse.repository.PresetRepository


class AudioFxImpl constructor(
        private val context: Context,
        private val presetRepository: PresetRepository
): AudioFxApplicable {

    companion object {
        private const val TAG = "AudioFxImpl"

        // const
        private const val MIN_BASS_STRENGTH: Short = 0
        private const val MAX_BASS_STRENGTH: Short = 999
        private const val MIN_VIRTUALIZER_LEVEL: Short = 0
        private const val MAX_VIRTUALIZER_LEVEL: Short = 999

        private val PRESET_REVERB_INDEXES = shortArrayOf(
                PresetReverb.PRESET_NONE, PresetReverb.PRESET_LARGEHALL, PresetReverb.PRESET_LARGEROOM,
                PresetReverb.PRESET_MEDIUMHALL, PresetReverb.PRESET_MEDIUMROOM, PresetReverb.PRESET_PLATE,
                PresetReverb.PRESET_SMALLROOM)

        // storage name
        private const val STORAGE_NAME = "CustomPresetStorage"
        // keys
        private const val KEY_ENABLED = "enabled"
        private const val KEY_BANDS_HOLDER_STRING = "bar"
        private const val KEY_BASS_STRENGTH = "bass_strength"
        private const val KEY_VIRTUALIZER_STRENGTH = "virtualizer_strength"
        private const val KEY_USE_NATIVE_PRESET = "use_native_preset"
        private const val KEY_LAST_NATIVE_PRESET = "last_native_preset"
        private const val KEY_USE_CUSTOM_PRESET = "use_custom_preset"
        private const val KEY_LAST_CUSTOM_PRESET_ID = "last_custom_preset_id"
        private const val KEY_LAST_PRESET_REVERB = "last_preset_reverb"

        private fun ShortArray?.convertToString(): String? = this?.run {
            val length = size
            val sb = StringBuilder()
            for (i in 0 until length) {
                sb.append(get(i).toInt())
                if (i < length - 1) sb.append(',')
            }
            sb.toString()
        }

        private fun String?.convertToPreset(): ShortArray = this?.run {
            return try {
                val barStrings = this.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val length = barStrings.size
                val levels = ShortArray(length)
                for (i in 0 until length) {
                    levels[i] = java.lang.Short.valueOf(barStrings[i])
                }
                levels
            } catch (t: Throwable) {
                ShortArray(5)
            }
        } ?: ShortArray(5)

        private fun checkInRange(value: Short, min: Short, max: Short): Short {
            return when {
                value < min -> min
                value > max -> max
                else -> value
            }
        }
    }

    private val eventHandler = Handler(Looper.getMainLooper())

    private val storage = context.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE)

    private var enabled = false

    // Holds current levels array of equalizer bands
    private var bandsHolder: ShortArray? = null

    private var useNativePreset = false
    private var nativePresetIndex: Short = 0

    private var useCustomPreset = false
    private var customPreset: CustomPreset? = null
    private var customPresetId: Long = CustomPreset.NO_ID

    private var bassStrength: Short = 0

    private var virtualizerStrength: Short = 0

    private var presetReverbIndex: Short = PresetReverb.PRESET_NONE

    // Observers
    private val observers = mutableSetOf<AudioFxObserver>()

    // Remember the last session id given as parameter in AudioFxImpl.apply(sessionId) method.
    // If it doesn't differ from the value given in AudioFxImpl.apply(sessionId) method,
    // Then we don't need to initialize audio effects again.
    private var lastSessionId: Int? = null
    private var equalizer: android.media.audiofx.Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var presetReverb: PresetReverb? = null

    private val hasEqualizer: Boolean
    private val hasBassBoost: Boolean
    private val hasVirtualizer: Boolean
    private val hasPresetReverb: Boolean

    init {
        var hasEqualizer = false
        var hasBassBoost = false
        var hasVirtualizer = false
        var hasPresetReverb = false

        // Retrieving descriptors of all audio effects in this device
        val descriptors = try {
            AudioEffect.queryEffects()
        } catch (e: Throwable) {
            log(e)
            null
        }

        // Checking what audio effects the device does have
        if (descriptors != null) {
            for (i in descriptors.indices) {
                when (descriptors[i].type) {
                    AudioEffect.EFFECT_TYPE_EQUALIZER -> hasEqualizer = true
                    AudioEffect.EFFECT_TYPE_BASS_BOOST -> hasBassBoost = true
                    AudioEffect.EFFECT_TYPE_VIRTUALIZER -> hasVirtualizer = true
                    AudioEffect.EFFECT_TYPE_PRESET_REVERB -> hasPresetReverb = true
                }
            }
        }

        this.hasEqualizer = hasEqualizer
        this.hasBassBoost = hasBassBoost
        this.hasVirtualizer = hasVirtualizer
        this.hasPresetReverb = hasPresetReverb

        restoreInternal()
    }

    /********************************
     ******* INTERNAL METHODS *******
     *******************************/
    private fun isDebug() = BuildConfig.DEBUG

    private fun log(msg: String) {
        Trace.d("AudioFxImpl", msg)
    }

    private fun log(t: Throwable) {
        Trace.e("AudioFxImpl", t)
    }

    private fun adjustBandsHolderFromEqualizer() {
        equalizer?.also { eq ->
            try {
                val length = eq.numberOfBands.toInt()
                val levels = bandsHolder.let { array ->
                    if (array == null || array.size != length) {
                        ShortArray(length)
                    } else array
                }
                for (i in 0 until length) {
                    levels[i] = eq.getBandLevel(i.toShort())
                }
                bandsHolder = levels
            } catch (e: Exception) {
                log(e)
            }
        }
    }

    private fun restoreInternal() {
        log("Restoring settings")
        dumpStorage()

        // bands holder
        when {
            storage.contains(KEY_BANDS_HOLDER_STRING) -> {
                val presetString = storage.getString(KEY_BANDS_HOLDER_STRING, "")
                bandsHolder = presetString.convertToPreset()
            }
            else -> adjustBandsHolderFromEqualizer()
        }

        // enabled status
        enabled = storage.getBoolean(KEY_ENABLED, false)

        // native preset
        useNativePreset = storage.getBoolean(KEY_USE_NATIVE_PRESET, false)
        nativePresetIndex = storage.getInt(KEY_LAST_NATIVE_PRESET, 0).toShort()

        // custom preset
        useCustomPreset = storage.getBoolean(KEY_USE_CUSTOM_PRESET, false)
        customPresetId = storage.getLong(KEY_LAST_CUSTOM_PRESET_ID, CustomPreset.NO_ID)

        // bass strength
        bassStrength = storage.getInt(KEY_BASS_STRENGTH, 0).toShort()
        bassStrength = checkInRange(bassStrength, MIN_BASS_STRENGTH, MAX_BASS_STRENGTH)

        // virtualizer strength
        virtualizerStrength = storage.getInt(KEY_VIRTUALIZER_STRENGTH, 0).toShort()
        virtualizerStrength = checkInRange(virtualizerStrength, MIN_VIRTUALIZER_LEVEL, MAX_VIRTUALIZER_LEVEL)

        // preset reverb
        presetReverbIndex = storage.getInt(KEY_LAST_PRESET_REVERB, PresetReverb.PRESET_NONE.toInt()).toShort()
        if (PRESET_REVERB_INDEXES.contains(presetReverbIndex).not()) { // validating preset reverb index
            presetReverbIndex = PresetReverb.PRESET_NONE
        }

        log("Settings restored!")
    }

    /**
     * Saves settings of all controllers to the local preference storage
     */
    private fun saveInternal() {
        log("Saving settings")
        storage.edit()
                .putBoolean(KEY_ENABLED, enabled)
                // native preset
                .putBoolean(KEY_USE_NATIVE_PRESET, useNativePreset)
                .putInt(KEY_LAST_NATIVE_PRESET, nativePresetIndex.toInt())
                // custom preset
                .putBoolean(KEY_USE_CUSTOM_PRESET, useCustomPreset)
                .putLong(KEY_LAST_CUSTOM_PRESET_ID, customPresetId)
                // bands holder
                .putString(KEY_BANDS_HOLDER_STRING, bandsHolder.convertToString())
                // bass
                .putInt(KEY_BASS_STRENGTH, bassStrength.toInt())
                // virtualizer
                .putInt(KEY_VIRTUALIZER_STRENGTH, virtualizerStrength.toInt())
                // reverb
                .putInt(KEY_LAST_PRESET_REVERB, presetReverbIndex.toInt())
                .apply()
    }

    /**
     * (Re)initializes all audio fx controllers for the given session id
     */
    private fun initInternal(sessionId: Int) {
        // if there is no the old session or the old session doesn't equal the current one
        if (lastSessionId == null || lastSessionId != sessionId) {

            val priority = 0

            try {
                equalizer?.release()
                log("Initializing equalizer")
                equalizer = android.media.audiofx.Equalizer(priority, sessionId)
                equalizer?.enabled = enabled
            } catch (t: Throwable) {
                log(t)
            }

            try {
                bassBoost?.release()
                log("Initializing bass boost")
                bassBoost = BassBoost(priority, sessionId)
                bassBoost?.enabled = enabled
            } catch (t: Throwable) {
                log(t)
            }

            try {
                virtualizer?.release()
                log("Initializing virtualizer")
                virtualizer = Virtualizer(priority, sessionId)
                virtualizer?.enabled = enabled
            } catch (t: Throwable) {
                log(t)
            }

            try {
                presetReverb?.release()
                log("Initializing preset reverb")
                presetReverb = PresetReverb(priority, sessionId)
                presetReverb?.enabled = enabled
            } catch (t: Throwable) {
                log(t)
            }

            lastSessionId = sessionId
        }
    }

    override fun apply(audioSessionId: Int) {
        initInternal(audioSessionId)

        when {
            useNativePreset -> try {
                equalizer?.usePreset(nativePresetIndex)
            } catch (e: Throwable) {
                log(e)
            }

            useCustomPreset -> try {
                val preset = customPreset.let { currCustomPreset ->
                    if (currCustomPreset != null) currCustomPreset
                    else {
                        val preset = try {
                            presetRepository
                                    .getPresetById(customPresetId)
                                    .firstOrError()
                                    .blockingGet()
                        } catch (e: Throwable) {
                            log(e)
                            null
                        }
                        customPreset = preset
                        preset
                    }
                }
                equalizer?.also { safeEqualizer ->
                    preset?.also { safePreset ->
                        val levels = safePreset.levels
                        for (i in levels.indices) {
                            try {
                                safeEqualizer.setBandLevel(i.toShort(), levels[i])
                            } catch (exc: Exception) {
                                log(exc)
                            }
                        }
                    }
                }
            } catch (e: Throwable) {
                log(e)
            }

            else -> bandsHolder?.let { safeLevels ->
                for (i in safeLevels.indices) {
                    try {
                        equalizer?.setBandLevel(i.toShort(), safeLevels[i])
                    } catch (e: Throwable) {
                        log(e)
                    }
                }
            }
        }

        try {
            bassBoost?.setStrength(bassStrength)
        } catch (t: Throwable) {
            log(t)
        }

        try {
            virtualizer?.setStrength(virtualizerStrength)
        } catch (t: Throwable) {
            log(t)
        }

        try {
            presetReverb?.preset = presetReverbIndex
        } catch (t: Throwable) {
            log(t)
        }

        log("Applied!")
    }

    override fun save() {
        saveInternal()
    }

    override fun registerObserver(observer: AudioFxObserver) {
        ThreadStrictMode.assertMain()
        log("Registering observer")
        observers.add(observer)
    }

    override fun unregisterObserver(observer: AudioFxObserver) {
        ThreadStrictMode.assertMain()
        log("Unregistering observer")
        observers.remove(observer)
    }

    override fun isAvailable() = true

    override fun isEnabled() = enabled

    override fun setEnabled(enabled: Boolean) {
        this.enabled = enabled

        try {
            equalizer?.enabled = enabled
        } catch (e: Throwable) {
            log(e)
        }

        try {
            bassBoost?.enabled = enabled
        } catch (e: Throwable) {
            log(e)
        }

        try {
            virtualizer?.enabled = enabled
        } catch (e: Throwable) {
            log(e)
        }

        try {
            presetReverb?.enabled = enabled
        } catch (e: Throwable) {
            log(e)
        }

        if (enabled) {
            dispatchEnabled()
        } else {
            dispatchDisabled()
        }
    }

    override fun hasEqualizer(): Boolean = hasEqualizer

    override fun getMinBandLevelRange(): Short {
        return try {
            equalizer?.bandLevelRange?.get(0) ?: 0
        } catch (e: Throwable) {
            log(e)
            0
        }
    }

    override fun getMaxBandLevelRange(): Short {
        return try {
            equalizer?.bandLevelRange?.get(1) ?: 0
        } catch (e: Throwable) {
            log(e)
            0
        }
    }

    private fun getDefaultBandFreqRange(band: Short): IntArray {
        return when(band) {
            0.toShort() -> intArrayOf(30_000, 120_000)
            1.toShort() -> intArrayOf(120_001, 460_000)
            2.toShort() -> intArrayOf(460_001, 1_800_00)
            3.toShort() -> intArrayOf(1_800_001, 7_000_000)
            4.toShort() -> intArrayOf(7_000_000, 20_000_000)
            else -> intArrayOf(7_000_000, 20_000_000)
        }
    }

    override fun getBandFreqRange(band: Short): IntArray {
        return try {
            equalizer?.getBandFreqRange(band) ?: getDefaultBandFreqRange(band)
        } catch (e: Throwable) {
            log(e)
            getDefaultBandFreqRange(band)
        }
    }

    override fun getNumberOfBands(): Short {
        return try {
            equalizer?.numberOfBands ?: 0
        } catch (e: Throwable) {
            log(e)
            0
        }
    }

    override fun getBandLevel(band: Short): Short {
        return try {
            equalizer?.getBandLevel(band) ?: 0
        } catch (e: Throwable) {
            log(e)
            0
        }
    }

    override fun setBandLevel(band: Short, level: Short) {
        try {
            useNativePreset = false
            nativePresetIndex = NativePreset.NO_INDEX

            useCustomPreset = false
            customPreset = null
            customPresetId = CustomPreset.NO_ID

            bandsHolder?.set(band.toInt(), level)
            equalizer?.setBandLevel(band, level)
        } catch (t: Throwable) {
            log(t)
        } finally {
            dispatchBandLevelChanged(band, level)
        }
    }

    override fun getNumberOfPresets(): Short {
        return try {
            equalizer?.numberOfPresets ?: 0
        } catch (e: Throwable) {
            log(e)
            0
        }
    }

    override fun getPresetName(index: Short): String {
        return try {
            equalizer?.getPresetName(index) ?: ""
        } catch (e: Throwable) {
            log(e)
            ""
        }
    }

    override fun useNativePreset(preset: NativePreset) {
        try {
            useNativePreset = true
            nativePresetIndex = preset.index

            useCustomPreset = false
            customPreset = null
            customPresetId = CustomPreset.NO_ID

            equalizer?.usePreset(preset.index)
            // let custom preset set up itself from the currently used native preset
            adjustBandsHolderFromEqualizer()
        } catch (t: Throwable) {
            log(t)
        } finally {
            dispatchPresetUsed(preset)
        }
    }

    override fun unusePreset() {
        useNativePreset = false
        nativePresetIndex = NativePreset.NO_INDEX

        useCustomPreset = false
        customPreset = null
        customPresetId = CustomPreset.NO_ID
    }

    override fun useCustomPreset(preset: CustomPreset) {
        val levels = preset.levels
        bandsHolder = levels

        useNativePreset = false
        nativePresetIndex = NativePreset.NO_INDEX

        useCustomPreset = true
        customPreset = preset
        customPresetId = preset.id

        equalizer?.also { safeEqualizer ->
            for (i in levels.indices) {
                try {
                    safeEqualizer.setBandLevel(i.toShort(), levels[i])
                } catch (exc: Exception) {
                    log(exc)
                    // See https://fabric.io/frolovs-projects/android/apps/com.frolo.musp/issues/5b7b02466007d59fcd56d949?time=last-seven-days
                }
            }
        }
        // let custom preset set up itself from the currently used custom preset
        adjustBandsHolderFromEqualizer()
        dispatchPresetUsed(preset)
    }

    override fun isUsingNativePreset() = useNativePreset

    override fun isUsingCustomPreset() = useCustomPreset

    override fun getCurrentNativePreset(): NativePreset? {
        return if (useNativePreset) {
            NativePreset(nativePresetIndex, "")
        } else null
    }

    override fun getCurrentCustomPreset(): CustomPreset? {
        return customPreset
    }

    override fun hasPresetReverb(): Boolean = hasPresetReverb

    override fun getNumberOfPresetReverbs(): Short {
        return PRESET_REVERB_INDEXES.size.toShort()
    }

    override fun getPresetReverbIndexes(): ShortArray {
        return PRESET_REVERB_INDEXES.copyOf()
    }

    override fun getPresetReverbName(index: Short): String {
        return when(index) {
            PresetReverb.PRESET_NONE -> context.getString(R.string.preset_reverb_none)
            PresetReverb.PRESET_LARGEHALL -> context.getString(R.string.preset_reverb_large_hall)
            PresetReverb.PRESET_LARGEROOM -> context.getString(R.string.preset_reverb_large_room)
            PresetReverb.PRESET_MEDIUMHALL -> context.getString(R.string.preset_reverb_medium_hall)
            PresetReverb.PRESET_MEDIUMROOM -> context.getString(R.string.preset_reverb_medium_room)
            PresetReverb.PRESET_PLATE -> context.getString(R.string.preset_reverb_plate)
            PresetReverb.PRESET_SMALLROOM -> context.getString(R.string.preset_reverb_small_rooom)
            else -> ""
        }
    }

    override fun usePresetReverb(index: Short) {
        try {
            presetReverbIndex = index
            presetReverb?.preset = index
        } catch (t: Throwable) {
            log(t)
        } finally {
            dispatchPresetReverbUsed(index)
        }
    }

    override fun getCurrentPresetReverb(): Short {
        return try {
            presetReverb?.preset ?: PresetReverb.PRESET_NONE
        } catch (t: Throwable) {
            log(t)
            PresetReverb.PRESET_NONE
        }
    }

    override fun hasBassBoost(): Boolean = hasBassBoost

    override fun getMinBassStrength() = MIN_BASS_STRENGTH

    override fun getMaxBassStrength() = MAX_BASS_STRENGTH

    override fun getBassStrength(): Short {
        return try {
            bassBoost?.roundedStrength ?: 0.toShort()
        } catch (e: Throwable) {
            log(e)
            0
        }
    }

    override fun setBassStrength(strength: Short) {
        try {
            bassStrength = checkInRange(strength, MIN_BASS_STRENGTH, MAX_BASS_STRENGTH)
            bassBoost?.setStrength(bassStrength)
        } catch (e: Throwable) {
            log(e)
        } finally {
            dispatchBassBoostChanged(bassStrength)
        }
    }

    override fun hasVirtualizer(): Boolean = hasVirtualizer

    override fun getMinVirtualizerStrength() = MIN_VIRTUALIZER_LEVEL

    override fun getMaxVirtualizerStrength() = MAX_VIRTUALIZER_LEVEL

    override fun getVirtualizerStrength(): Short {
        return try {
            virtualizer?.roundedStrength ?: 0
        } catch (e: Throwable) {
            log(e)
            0
        }
    }

    override fun setVirtualizerStrength(strength: Short) {
        try {
            virtualizerStrength = checkInRange(strength, MIN_VIRTUALIZER_LEVEL, MAX_VIRTUALIZER_LEVEL)
            virtualizer?.setStrength(virtualizerStrength)
        } catch (e: Throwable) {
            log(e)
        } finally {
            dispatchVirtualizerStrengthChanged(virtualizerStrength)
        }
    }

    private fun dumpStorage() {
        if (isDebug()) {
            val storageInfo = storage.all.let { map ->
                map.entries.fold(StringBuilder("Storage entries:")) { builder, item ->
                    builder.append('\n').append('[').append(item.key).append('=').append(item.value).append(']')
                }
            }
            log(storageInfo.toString())
        }
    }

    /********************************
     ****** DISPATCHER METHODS ******
     *******************************/
    @MainThread
    private fun notifyObservers(action: (AudioFxObserver) -> Unit) {
        if (eventHandler.looper.thread == Thread.currentThread()) {
            observers.forEach(action)
        } else {
            eventHandler.post { observers.forEach(action) }
        }
    }

    private fun dispatchEnabled() {
        notifyObservers { it.onEnabled(this) }
    }

    private fun dispatchDisabled() {
        notifyObservers { it.onDisabled(this) }
    }

    private fun dispatchBandLevelChanged(band: Short, level: Short) {
        notifyObservers { it.onBandLevelChanged(this, band, level) }
    }

    private fun dispatchBassBoostChanged(boost: Short) {
        notifyObservers { it.onBassStrengthChanged(this, boost) }
    }

    private fun dispatchVirtualizerStrengthChanged(strength: Short) {
        notifyObservers { it.onVirtualizerStrengthChanged(this, strength) }
    }

    private fun dispatchPresetUsed(preset: Preset) {
        notifyObservers { it.onPresetUsed(this, preset) }
    }

    private fun dispatchPresetReverbUsed(presetReverbIndex: Short) {
        notifyObservers { it.onPresetReverbUsed(this, presetReverbIndex) }
    }
}