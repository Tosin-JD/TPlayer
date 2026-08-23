package com.tosin.musicplayer.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import java.nio.ByteBuffer

/**
 * Shared, thread-safe state driving [CrossfadeAudioProcessor].
 *
 * The service is the authority on the current track boundaries and updates these
 * values as playback advances; the audio thread reads them on every flush and frame.
 */
class CrossfadeState {
    @Volatile var enabled: Boolean = false
    @Volatile var fadeDurationUs: Long = 0L
    @Volatile var seekPositionUs: Long = 0L
    @Volatile var trackDurationUs: Long = C.TIME_UNSET
}

/**
 * Fades the beginning and end of each track in the queue, producing a smooth
 * crossfade-style transition between consecutive media items.
 *
 * The processor is passive (bit-identical passthrough) whenever the crossfade is
 * disabled. When enabled it applies a linear gain ramp over the first and last
 * [CrossfadeState.fadeDurationUs] of every track using the track boundary info the
 * service publishes on [CrossfadeState].
 */
@OptIn(UnstableApi::class)
class CrossfadeAudioProcessor(
    private val state: CrossfadeState
) : BaseAudioProcessor() {

    private var sampleRate: Int = 0
    private var encoding: Int = C.ENCODING_PCM_16BIT
    private var channelCount: Int = 2
    private var bytesPerFrame: Int = 0

    /** Frames read from the decoder since the audio pipeline was last reset. */
    private var readFrames: Long = 0L

    /** Length of the current track in frames; [C.TIME_UNSET] until known. */
    private var trackDurationFrames: Long = C.TIME_UNSET

    override fun onConfigure(inputFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        if (inputFormat.encoding != C.ENCODING_PCM_16BIT &&
            inputFormat.encoding != C.ENCODING_PCM_24BIT &&
            inputFormat.encoding != C.ENCODING_PCM_32BIT &&
            inputFormat.encoding != C.ENCODING_PCM_FLOAT
        ) {
            return AudioProcessor.AudioFormat.NOT_SET
        }
        sampleRate = inputFormat.sampleRate
        encoding = inputFormat.encoding
        channelCount = inputFormat.channelCount
        bytesPerFrame = inputFormat.bytesPerFrame
        
        // Validate bytesPerFrame calculation
        if (bytesPerFrame <= 0) {
            return AudioProcessor.AudioFormat.NOT_SET
        }
        
        return inputFormat
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onFlush() {
        readFrames = toFrames(state.seekPositionUs)
        trackDurationFrames = toFrames(state.trackDurationUs)
    }

    override fun onReset() {
        readFrames = 0L
        trackDurationFrames = C.TIME_UNSET
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        if (!inputBuffer.hasRemaining()) return
        if (bytesPerFrame <= 0) {
            throw IllegalStateException("CrossfadeAudioProcessor must be configured before queueInput")
        }

        val inputFrames = inputBuffer.remaining() / bytesPerFrame
        val outputBuffer = replaceOutputBuffer(inputBuffer.remaining())

        if (!state.enabled || state.fadeDurationUs <= 0L) {
            outputBuffer.put(inputBuffer)
            outputBuffer.flip()
            readFrames += inputFrames
            return
        }

        var framesProcessed = 0
        while (inputBuffer.hasRemaining() && framesProcessed < inputFrames) {
            syncTrackPosition()
            val gain = gainAtPosition(readFrames)
            for (channel in 0 until channelCount) {
                when (encoding) {
                    C.ENCODING_PCM_16BIT -> outputBuffer.putShort(
                        (inputBuffer.short * gain).toInt().coerceIn(-32768, 32767).toShort()
                    )
                    C.ENCODING_PCM_FLOAT -> outputBuffer.putFloat((inputBuffer.float * gain).coerceIn(-1.0f, 1.0f))
                    C.ENCODING_PCM_24BIT -> {
                        val sample = (inputBuffer.get().toInt() and 0xFF) or
                                ((inputBuffer.get().toInt() and 0xFF) shl 8) or
                                (inputBuffer.get().toInt() shl 16)
                        val scaled = (sample * gain).toInt()
                        outputBuffer.put((scaled and 0xFF).toByte())
                        outputBuffer.put(((scaled shr 8) and 0xFF).toByte())
                        outputBuffer.put(((scaled shr 16) and 0xFF).toByte())
                    }
                    C.ENCODING_PCM_32BIT -> outputBuffer.putInt((inputBuffer.int * gain).toInt())
                    else -> outputBuffer.put(inputBuffer.get())
                }
            }
            readFrames++
            framesProcessed++
        }
        outputBuffer.flip()
    }

    private fun syncTrackPosition() {
        val currentTrackDurationUs = state.trackDurationUs
        if (currentTrackDurationUs != C.TIME_UNSET && currentTrackDurationUs > 0L) {
            val latestDurationFrames = toFrames(currentTrackDurationUs)
            if (trackDurationFrames != latestDurationFrames) {
                trackDurationFrames = latestDurationFrames
            }
        }
    }

    private fun gainAtPosition(positionFrames: Long): Float {
        if (!state.enabled || state.fadeDurationUs <= 0L || sampleRate <= 0) return 1.0f

        val currentTrackDurationUs = state.trackDurationUs
        val durationFrames = if (currentTrackDurationUs != C.TIME_UNSET && currentTrackDurationUs > 0L) {
            toFrames(currentTrackDurationUs)
        } else {
            trackDurationFrames
        }

        // If duration is unknown or invalid, don't apply any gain modifications
        if (durationFrames <= 0L || durationFrames == C.TIME_UNSET) {
            return 1.0f
        }

        val posInTrack = positionFrames.coerceAtLeast(0L)
        val fadeFrames = (state.fadeDurationUs * sampleRate / 1_000_000L).coerceAtLeast(1L)

        // Fade in at the start of the track
        val fadeIn = if (posInTrack >= fadeFrames) 1.0f else (posInTrack.toFloat() / fadeFrames.toFloat()).coerceIn(0.0f, 1.0f)

        // Fade out at the end of the track
        val remaining = durationFrames - posInTrack
        val fadeOut = when {
            remaining >= fadeFrames -> 1.0f  // Still far from track end
            remaining > 0L -> (remaining.toFloat() / fadeFrames.toFloat()).coerceIn(0.0f, 1.0f)  // In fade-out zone
            else -> 1.0f  // Past expected track end frame bound before next transition: default to full volume to avoid silencing buffer tail
        }

        return (fadeIn * fadeOut).coerceIn(0.0f, 1.0f)
    }

    private fun toFrames(durationUs: Long): Long {
        if (durationUs == C.TIME_UNSET || durationUs < 0L || sampleRate <= 0) {
            return if (durationUs == C.TIME_UNSET) C.TIME_UNSET else 0L
        }
        return Util.durationUsToSampleCount(durationUs, sampleRate)
    }
}

/**
 * Injects [CrossfadeAudioProcessor] into the playback audio pipeline. Media3 1.10 no
 * longer exposes an ExoPlayer-level audio sink hook, so the processors are attached
 * through a [DefaultRenderersFactory] subclass that overrides [buildAudioSink].
 */
@OptIn(UnstableApi::class)
class CrossfadeRenderersFactory(
    context: Context,
    private val crossfadeState: CrossfadeState
) : DefaultRenderersFactory(context) {

    override fun buildAudioSink(
        context: Context,
        enableFloatOutput: Boolean,
        enableAudioOutputPlaybackParameters: Boolean
    ): AudioSink {
        return DefaultAudioSink.Builder(context)
            .setEnableFloatOutput(enableFloatOutput)
            .setEnableAudioOutputPlaybackParameters(enableAudioOutputPlaybackParameters)
            .setAudioProcessors(arrayOf(CrossfadeAudioProcessor(crossfadeState)))
            .build()
    }
}
