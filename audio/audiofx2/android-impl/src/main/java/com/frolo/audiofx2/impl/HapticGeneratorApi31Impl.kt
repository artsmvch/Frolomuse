package com.frolo.audiofx2.impl

import android.content.Context
import android.os.Build
import androidx.annotation.GuardedBy
import androidx.annotation.RequiresApi
import com.frolo.audiofx2.AudioEffect2
import com.frolo.audiofx2.AudioEffectDescriptor
import com.frolo.audiofx2.HapticGenerator
import android.media.audiofx.HapticGenerator as AndroidHapticGenerator

@RequiresApi(Build.VERSION_CODES.S)
internal class HapticGeneratorApi31Impl(
    private val context: Context,
    private val errorHandler: AudioEffect2ErrorHandler
): HapticGenerator, BaseAudioEffect2Impl<AndroidHapticGenerator>(), AudioFx2AttachProtocol {
    override val descriptor: AudioEffectDescriptor
        get() = SimpleAudioEffectDescriptor("HapticGenerator")

    override var isEnabled: Boolean
        get() = synchronized(lock) {
            return engine
                ?.runCatching { enabled }
                ?.onFailure { errorHandler.onAudioEffectError(this, it) }
                ?.getOrNull() ?: false
        }
        set(value) = synchronized(lock) {
            engine
                ?.runCatching { setEnabledOrThrow(value) }
                ?.onFailure { errorHandler.onAudioEffectError(this, it) }
            enableStatusChangeListenerRegistry.dispatchEnableStatusChange(value)
        }

    private val lock = Any()
    @GuardedBy("lock")
    private var engine: AndroidHapticGenerator? = null

    private val enableStatusChangeListenerRegistry =
        EnableStatusChangeListenerRegistry(context, this)

    override fun addOnEnableStatusChangeListener(listener: AudioEffect2.OnEnableStatusChangeListener) {
        enableStatusChangeListenerRegistry.addListener(listener)
    }

    override fun removeOnEnableStatusChangeListener(listener: AudioEffect2.OnEnableStatusChangeListener) {
        enableStatusChangeListenerRegistry.removeListener(listener)
    }

    override fun onAttachTo(target: AudioFx2AttachTarget) = synchronized(lock) {
        try {
            engine?.release()
            engine = null
        } catch (err: Throwable) {
            errorHandler.onAudioEffectError(this, err)
        }
        try {
            val newEngine = AndroidHapticGenerator.create(target.sessionId)
            newEngine.enabled = isEnabled
            this.engine = newEngine
        } catch (err: Throwable) {
            errorHandler.onAudioEffectError(this, err)
        }
    }

    override fun onRelease() = synchronized(lock) {
        try {
            engine?.release()
            engine = null
        } catch (err: Throwable) {
            errorHandler.onAudioEffectError(this, err)
        }
    }
}