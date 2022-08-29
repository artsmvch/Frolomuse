package com.frolo.audiofx2.ui

import android.app.Dialog
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import com.frolo.audiofx2.AudioFx2
import com.frolo.audiofx2.ui.controlpanel.AttachInfoDialog
import com.frolo.audiofx2.ui.controlpanel.AudioFxControlPanelFragment

object AudioFx2Feature {
    private var input: AudioFx2FeatureInput? = null

    fun init(input: AudioFx2FeatureInput) {
        this.input = input
    }

    private fun requireInput(): AudioFx2FeatureInput {
        return this.input ?: throw NullPointerException(
            "AudioFx2FeatureInput not found. Make sure you have AudioFx2Feature initialized")
    }

    internal fun getAudioFx2(): AudioFx2 {
        return requireInput().audioFx2
    }

    internal fun getAttachInfoLiveData(): LiveData<AudioFx2AttachInfo> {
        return requireInput().audioFx2AttachInfo
    }

    fun createControlPanelFragment(): Fragment {
        return AudioFxControlPanelFragment.newInstance()
    }

    fun createAttachInfoDialog(context: Context, info: AudioFx2AttachInfo): Dialog {
        return AttachInfoDialog(context, info)
    }
}