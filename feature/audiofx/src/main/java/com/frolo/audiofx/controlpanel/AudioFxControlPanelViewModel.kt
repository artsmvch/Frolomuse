package com.frolo.audiofx.controlpanel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.frolo.audiofx.AudioFx
import com.frolo.audiofx.AudioFxFeature


class AudioFxControlPanelViewModel: ViewModel() {
    val audioFx: LiveData<AudioFx> by lazy {
        MutableLiveData(AudioFxFeature.getAudioFx())
    }
}