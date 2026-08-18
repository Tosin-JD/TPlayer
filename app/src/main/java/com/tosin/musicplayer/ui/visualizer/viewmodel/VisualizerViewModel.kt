package com.tosin.musicplayer.ui.visualizer.viewmodel

import android.media.audiofx.Visualizer
import androidx.lifecycle.ViewModel
import com.tosin.musicplayer.player.EqualizerManager
import com.tosin.musicplayer.ui.visualizer.FftProcessor
import com.tosin.musicplayer.ui.visualizer.VisualizationStyle
import com.tosin.musicplayer.ui.visualizer.VisualizerType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max

private const val BAR_COUNT = 48

/**
 * Holds visualizer lifecycle, FFT processing state, and the active visualization style.
 * Exposes [magnitudes] and [currentType] for the UI layer to consume.
 */
class VisualizerViewModel : ViewModel() {

    private val _magnitudes = MutableStateFlow(FloatArray(BAR_COUNT))
    val magnitudes: StateFlow<FloatArray> = _magnitudes.asStateFlow()

    private val _currentType = MutableStateFlow(VisualizerType.DEFAULT)
    val currentType: StateFlow<VisualizerType> = _currentType.asStateFlow()

    private var _currentStyle: VisualizationStyle = VisualizerType.DEFAULT.factory()
    val currentStyle: VisualizationStyle get() = _currentStyle

    private var nativeVisualizer: Visualizer? = null

    /**
     * Attaches to the current audio session and begins FFT capture.
     * Safe to call repeatedly — releases previous session first.
     */
    fun attachAudioSession(playing: Boolean) {
        releaseVisualizer()
        val sessionId = EqualizerManager.currentAudioSessionId
        if (sessionId <= 0 || !playing) return

        runCatching {
            nativeVisualizer = Visualizer(sessionId).apply {
                captureSize = Visualizer.getCaptureSizeRange()[1]
                setDataCaptureListener(
                    object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(
                            v: Visualizer?, w: ByteArray?, r: Int
                        ) = Unit

                        override fun onFftDataCapture(
                            v: Visualizer?, fft: ByteArray?, rate: Int
                        ) {
                            _magnitudes.value = FftProcessor.process(
                                fft, _magnitudes.value, BAR_COUNT
                            )
                        }
                    },
                    max(1000, Visualizer.getMaxCaptureRate() / 4),
                    false, true
                )
                enabled = true
            }
        }
    }

    /** Switches the active visualization style at runtime. */
    fun setVisualizerType(type: VisualizerType) {
        _currentType.value = type
        _currentStyle = type.factory()
    }

    /** Decays magnitudes toward zero (called per-frame when paused). */
    fun decayMagnitudes() {
        _magnitudes.value = FftProcessor.decay(_magnitudes.value)
    }

    fun releaseVisualizer() {
        runCatching { nativeVisualizer?.enabled = false }
        nativeVisualizer?.release()
        nativeVisualizer = null
    }

    override fun onCleared() {
        super.onCleared()
        releaseVisualizer()
    }
}
