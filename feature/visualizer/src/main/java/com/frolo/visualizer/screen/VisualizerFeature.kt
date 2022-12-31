package com.frolo.visualizer.screen

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.atomic.AtomicReference

object VisualizerFeature {
    private val inputRef = AtomicReference<VisualizerFeatureInput>()

    fun init(input: VisualizerFeatureInput) {
        inputRef.set(input)
    }

    fun createVisualizerFragment(): Fragment {
        return VisualizerFragment.newInstance()
    }

    internal fun getAudioSessionId(): LiveData<Int> {
        return inputRef.get()?.audioSessionId ?: MutableLiveData<Int>(0)
    }

    internal fun getRendererTypes(): List<VisualizerRendererType> {
        return inputRef.get()?.rendererTypes ?: VisualizerRendererType.values().toList()
    }

    internal fun getDefaultRendererType(): VisualizerRendererType {
        return inputRef.get()?.defaultRendererType ?: VisualizerRendererType.LINE_SPECTRUM
    }
}