package com.frolo.audiofx2.ui.controlpanel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.frolo.audiofx2.ui.AudioFx2AttachInfo
import com.frolo.audiofx2.ui.AudioFx2Feature
import com.frolo.audiofx2.*
import com.frolo.rx.KeyedDisposableContainer


internal class AudioFxControlPanelViewModel(
    application: Application
): AndroidViewModel(application) {
    private val keyedDisposables = KeyedDisposableContainer<String>()

    val audioFx2: LiveData<AudioFx2> by lazy {
        MutableLiveData(AudioFx2Feature.getAudioFx2())
    }

    val attachInfo: LiveData<AudioFx2AttachInfo> get() =
        AudioFx2Feature.getAttachInfoLiveData()

    val equalizer: LiveData<Equalizer> =
        Transformations.map(audioFx2) { audioFx2 -> audioFx2?.equalizer}

    val bassBoost: LiveData<BassBoost> by lazy {
        Transformations.map(audioFx2) { audioFx2 -> audioFx2?.bassBoost }
    }
    val virtualizer: LiveData<Virtualizer> by lazy {
        Transformations.map(audioFx2) { audioFx2 -> audioFx2?.virtualizer }
    }
    val loudness: LiveData<Loudness> by lazy {
        Transformations.map(audioFx2) { audioFx2 -> audioFx2?.loudness }
    }

    val reverb: LiveData<Reverb> by lazy {
        Transformations.map(audioFx2) { audioFx2 -> audioFx2?.reverb }
    }

    override fun onCleared() {
        super.onCleared()
        keyedDisposables.dispose()
    }
}