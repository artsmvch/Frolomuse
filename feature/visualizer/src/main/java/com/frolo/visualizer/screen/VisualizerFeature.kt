package com.frolo.visualizer.screen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.atomic.AtomicReference

object VisualizerFeature {
    private val inputRef = AtomicReference<VisualizerFeatureInput>()

    fun init(input: VisualizerFeatureInput) {
        inputRef.set(input)
    }

    internal fun getAudioSessionId(): LiveData<Int> {
        return inputRef.get()?.audioSessionId ?: MutableLiveData<Int>(0)
    }
}