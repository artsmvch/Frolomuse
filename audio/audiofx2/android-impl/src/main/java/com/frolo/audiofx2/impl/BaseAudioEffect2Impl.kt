package com.frolo.audiofx2.impl

import com.frolo.audiofx2.AudioEffect2
import java.util.concurrent.atomic.AtomicReference

internal abstract class BaseAudioEffect2Impl<E: android.media.audiofx.AudioEffect>:
    AudioEffect2, AudioFx2AttachProtocol {
    private val lastAttachTargetRef = AtomicReference<AudioFx2AttachTarget>(null)

    protected open fun shouldReAttach(
        newTarget: AudioFx2AttachTarget,
        oldTarget: AudioFx2AttachTarget
    ): Boolean{
        return oldTarget.priority != newTarget.priority ||
                oldTarget.sessionId != newTarget.sessionId ||
                oldTarget.mediaPlayer != newTarget.mediaPlayer
    }

    final override fun attachTo(target: AudioFx2AttachTarget) {
        val lastTarget = lastAttachTargetRef.get()
        // Check if the target has been actually changed
        if (lastTarget == null || shouldReAttach(lastTarget, target)) {
            onAttachTo(target)
            lastAttachTargetRef.set(target)
        }
    }

    protected abstract fun onAttachTo(target: AudioFx2AttachTarget)

    final override fun release() {
        onRelease()
        lastAttachTargetRef.set(null)
    }

    protected abstract fun onRelease()
}