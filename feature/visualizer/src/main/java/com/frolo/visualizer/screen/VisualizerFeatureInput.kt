package com.frolo.visualizer.screen

import androidx.lifecycle.LiveData

interface VisualizerFeatureInput {
    val audioSessionId: LiveData<Int>
    val rendererTypes: List<VisualizerRendererType>
    val defaultRendererType: VisualizerRendererType
}