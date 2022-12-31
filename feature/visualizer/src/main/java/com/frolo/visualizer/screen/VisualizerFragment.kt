package com.frolo.visualizer.screen

import android.Manifest
import android.content.pm.PackageManager
import android.media.audiofx.Visualizer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.transition.Fade
import androidx.transition.TransitionManager
import com.frolo.logger.api.Logger
import com.frolo.visualizerview.CircleRenderer
import com.frolo.visualizerview.CircleSpectrumRenderer
import com.frolo.visualizerview.LineRenderer
import com.frolo.visualizerview.LineSpectrumRenderer
import com.frolo.visualizerview.SpectrumRenderer
import kotlinx.android.synthetic.main.fragment_visualizer.*
import kotlinx.android.synthetic.main.include_request_record_permission.*


internal class VisualizerFragment : Fragment() {

    private val onDataCaptureListener = object : Visualizer.OnDataCaptureListener {
        override fun onWaveFormDataCapture(
            visualizer: Visualizer?,
            waveform: ByteArray?,
            samplingRate: Int
        ) {
            visualizer_view?.setData(waveform)
        }

        override fun onFftDataCapture(visualizer: Visualizer?, fft: ByteArray?, samplingRate: Int) {
        }
    }
    private var visualizer: Visualizer? = null

    private val audioSessionIdObserver = Observer<Int> { sessionId ->
        initVisualizer(sessionId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_visualizer, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val renderTypesAdapter = VisualizerRendererTypeAdapter(VisualizerRendererType.values())
        renderer_type_spinner.adapter = renderTypesAdapter
        renderer_type_spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                renderTypesAdapter.getItemAt(position).also(::setVisualizerRendererType)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        setVisualizerRendererType(VisualizerRendererType.LINE)
        request_permission_button.setOnClickListener { requestPermission() }
        checkPermission()
    }

    override fun onStart() {
        super.onStart()
        checkPermission()
    }

    private fun checkPermission() {
        val isPermissionGranted = ActivityCompat.checkSelfPermission(
            requireContext(), RECORD_AUDIO_PERMISSION) == PackageManager.PERMISSION_GRANTED
        initLayout(isPermissionGranted)
    }

    private fun initLayout(isPermissionGranted: Boolean) {
        val transition = Fade().apply {
            duration = 200L
        }
        (view as? ViewGroup)?.also { scene ->
            TransitionManager.beginDelayedTransition(scene, transition)
        }
        request_permission_layout.isVisible = !isPermissionGranted
        visualizer_layout.isVisible = isPermissionGranted
        val audioSessionId = VisualizerFeature.getAudioSessionId()
        if (isPermissionGranted) {
            audioSessionId.observe(viewLifecycleOwner, audioSessionIdObserver)
        } else {
            audioSessionId.removeObserver(audioSessionIdObserver)
        }
    }

    private fun requestPermission() {
        requestPermissions(arrayOf(RECORD_AUDIO_PERMISSION), RC_REQUEST_RECORD_AUDIO_PERMISSION)
    }

    private fun releaseVisualizer() {
        visualizer?.release()
        visualizer = null
    }

    @RequiresPermission(RECORD_AUDIO_PERMISSION)
    private fun initVisualizer(sessionId: Int) {
        if (this.visualizer != null) {
            this.visualizer?.release()
            this.visualizer = null
        }
        if (view == null) {
            return
        }
        val visualizer = Visualizer(sessionId)
        val rate = Visualizer.getMaxCaptureRate()
        visualizer.captureSize = Visualizer.getCaptureSizeRange()[0]
        val result = visualizer.setDataCaptureListener(onDataCaptureListener, rate, true, false)
        if (result != Visualizer.SUCCESS) {
            val msg = when (result) {
                Visualizer.ERROR_NO_INIT -> "ERROR_NO_INIT"
                Visualizer.ERROR_BAD_VALUE -> "ERROR_BAD_VALUE"
                Visualizer.ERROR_INVALID_OPERATION -> "ERROR_INVALID_OPERATION"
                Visualizer.ERROR_NO_MEMORY -> "ERROR_NO_MEMORY"
                Visualizer.ERROR_DEAD_OBJECT -> "ERROR_DEAD_OBJECT"
                // Visualizer.ERROR,
                else -> "ERROR"
            }
            Logger.e(LOG_TAG, "Failed to set DataCaptureListener: $msg")
        }
        visualizer.enabled = true
        this.visualizer = visualizer
    }

    private fun setVisualizerRendererType(type: VisualizerRendererType) {
        val renderer = when (type) {
            VisualizerRendererType.CIRCLE -> CircleRenderer()
            VisualizerRendererType.CIRCLE_SPECTRUM -> CircleSpectrumRenderer()
            VisualizerRendererType.LINE -> LineRenderer()
            VisualizerRendererType.LINE_SPECTRUM -> LineSpectrumRenderer()
            VisualizerRendererType.SPECTRUM -> SpectrumRenderer()
        }
        visualizer_view?.renderer = renderer
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RC_REQUEST_RECORD_AUDIO_PERMISSION) {
            for (i in permissions.indices) {
                if (permissions[i] == RECORD_AUDIO_PERMISSION) {
                    initLayout(grantResults[i] == PackageManager.PERMISSION_GRANTED)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releaseVisualizer()
    }

    companion object {
        private const val RC_REQUEST_RECORD_AUDIO_PERMISSION = 1005

        private const val RECORD_AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO

        private const val LOG_TAG = "VisualizerFragment"

        fun newInstance(): Fragment {
            return VisualizerFragment()
        }
    }
}