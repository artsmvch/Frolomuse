package com.frolo.audiofx2.impl

import android.content.Context
import com.frolo.audiofx2.AudioEffect2

internal class EnableStatusChangeListenerRegistry(
    context: Context,
    private val effect: AudioEffect2
): ListenerRegistry<AudioEffect2.OnEnableStatusChangeListener>(context) {
    fun dispatchEnableStatusChange(enabled: Boolean) = doDispatch { listener ->
        listener.onEnableStatusChange(
            effect = effect,
            isEnabled = enabled
        )
    }
}