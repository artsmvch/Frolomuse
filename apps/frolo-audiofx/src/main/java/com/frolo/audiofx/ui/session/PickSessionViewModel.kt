package com.frolo.audiofx.ui.session

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.frolo.audiofx.model.AudioSession
import com.frolo.audiofx.model.AudioSessionsLiveData

class PickSessionViewModel constructor(
    application: Application
): AndroidViewModel(application) {

    //private val _audioSessions = MutableLiveData<List<AudioSession>>()
    val audioSessions: LiveData<List<AudioSession>> = AudioSessionsLiveData(application)

    fun onAudioSessionPicked() {

    }
}