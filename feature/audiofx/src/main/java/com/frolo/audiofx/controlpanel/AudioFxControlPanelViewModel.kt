package com.frolo.audiofx.controlpanel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.frolo.audiofx.AudioFx2Feature
import com.frolo.audiofx2.AudioFx2


class AudioFxControlPanelViewModel: ViewModel() {
    val audioFx2: LiveData<AudioFx2> by lazy {
        MutableLiveData(AudioFx2Feature.getAudioFx2())
    }
}