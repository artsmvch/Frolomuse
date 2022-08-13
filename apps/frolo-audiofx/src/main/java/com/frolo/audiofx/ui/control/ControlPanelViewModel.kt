package com.frolo.audiofx.ui.control

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.frolo.audiofx.AudioFx

class ControlPanelViewModel constructor(
    private val audioFxInstance: AudioFx
): ViewModel() {

    val audioFx: LiveData<AudioFx> = MutableLiveData(audioFxInstance)

    @Suppress("UNCHECKED_CAST")
    class Factory constructor(
        private val audioFx: AudioFx
    ): ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass == ControlPanelViewModel::class.java) {
                return ControlPanelViewModel(audioFx) as T
            }
            return super.create(modelClass)
        }
    }
}