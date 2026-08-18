package com.tosin.musicplayer.ui.visualizer

import kotlin.math.sqrt

/**
 * Stateless utility that extracts normalized magnitudes from raw FFT bytes.
 *
 * Applies sqrt-shaping for perceptual balance and exponential smoothing
 * to reduce visual jitter between frames. Shared by all visualization styles (DRY).
 */
object FftProcessor {

    private const val SMOOTHING_FACTOR = 0.35f
    private const val MIN_FFT_SIZE = 4
    private const val DEFAULT_DECAY = 0.85f

    /**
     * Processes raw FFT bytes into [barCount] normalized magnitude bars (0f..1f).
     *
     * Each bar represents the peak magnitude across a range of frequency bins.
     * Applies sqrt-shaping for perceptual balance and exponential smoothing
     * against [previous] values to reduce frame-to-frame jitter.
     *
     * @param fft Raw FFT byte array from Android Visualizer API.
     * @param previous Previous frame's magnitudes for temporal smoothing.
     * @param barCount Number of output frequency bars.
     * @return New smoothed magnitude array.
     */
    fun process(fft: ByteArray?, previous: FloatArray, barCount: Int = 48): FloatArray {
        val result = previous.copyOf()
        if (fft == null || fft.size < MIN_FFT_SIZE) return result

        val binCount = fft.size / 2
        val scale = (fft.size / 4f).coerceAtLeast(1f)

        for (i in 0 until barCount) {
            val start = binCount * i / barCount
            val end = (binCount * (i + 1) / barCount).coerceAtLeast(start + 1)
            var peak = 0f

            for (bin in start until end) {
                if (bin * 2 + 1 >= fft.size) break
                val re = fft[bin * 2].toFloat()
                val im = fft[bin * 2 + 1].toFloat()
                val mag = sqrt(re * re + im * im) / scale
                if (mag > peak) peak = mag
            }

            val shaped = sqrt(peak.coerceIn(0f, 1f))
            result[i] += (shaped - result[i]) * SMOOTHING_FACTOR
        }
        return result
    }

    /**
     * Extracts low-frequency bass energy (0f..1f) to drive heartbeat pulses and rhythm bounces.
     */
    fun extractBeatEnergy(magnitudes: FloatArray): Float {
        if (magnitudes.isEmpty()) return 0f
        val bassBins = minOf(6, magnitudes.size)
        var sum = 0f
        for (i in 0 until bassBins) {
            sum += magnitudes[i]
        }
        return (sum / bassBins).coerceIn(0f, 1f)
    }

    /**
     * Decays all magnitudes toward zero when playback stops.
     * Creates a smooth fade-out effect instead of an abrupt visual cut.
     *
     * @param magnitudes Current magnitude values.
     * @param factor Decay multiplier per frame (0..1). Lower = faster decay.
     */
    fun decay(magnitudes: FloatArray, factor: Float = DEFAULT_DECAY): FloatArray {
        return FloatArray(magnitudes.size) { magnitudes[it] * factor }
    }
}
