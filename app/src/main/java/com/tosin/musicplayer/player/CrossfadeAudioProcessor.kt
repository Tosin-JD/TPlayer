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
import kotlin.math.min

/**
 * Shared, thread-safe state driving [CrossfadeAudioProcessor].
 *
 * The service is the authority on the current track boundaries and updates these
 * values as playback advances; the audio thread reads them on every flush and frame.
 */
class CrossfadeState {
    @Volatile var enabled: Boolean = false
    @Volatile var fadeDurationUs: Long = 0L
    @Volatile var trackStartUs: Long = 0L
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

    /** Stream frame at which the current track started. */
    private var trackStartFrames: Long = 0L

    /** Length of the current track in frames; [C.TIME_UNSET] until known. */
    private var trackDurationFrames: Long = C.TIME_UNSET

    override fun onConfigure(inputFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        if (inputFormat.encoding != C.ENCODING_PCM_16BIT &&
            inputFormat.encoding != C.ENCODING_PCM_FLOAT
        ) {
            return AudioProcessor.AudioFormat.NOT_SET
        }
        sampleRate = inputFormat.sampleRate
        encoding = inputFormat.encoding
        channelCount = inputFormat.channelCount
        bytesPerFrame = inputFormat.bytesPerFrame
        return inputFormat
    }

    override fun onFlush() {
        trackStartFrames = toFrames(state.trackStartUs)
        trackDurationFrames = toFrames(state.trackDurationUs)
        if (readFrames < trackStartFrames) {
            // A seek backwards or a track transition happened; re-sync the read cursor.
            readFrames = trackStartFrames
        } else if (trackDurationFrames > 0 && readFrames >= trackStartFrames + trackDurationFrames) {
            // The pipeline flushed after the current track had already ended and the
            // service hasn't published the new track start yet. Advance to the next track.
            trackStartFrames += trackDurationFrames
            trackDurationFrames = toFrames(state.trackDurationUs)
            readFrames = trackStartFrames
        }
    }

    override fun onReset() {
        readFrames = 0L
        trackStartFrames = 0L
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
            readFrames += inputFrames
            return
        }

        while (inputBuffer.hasRemaining()) {
            syncTrackPosition()
            val gain = gainAtPosition(readFrames)
            for (channel in 0 until channelCount) {
                when (encoding) {
                    C.ENCODING_PCM_16BIT -> outputBuffer.putShort(
                        (inputBuffer.short * gain).toInt().toShort()
                    )
                    C.ENCODING_PCM_FLOAT -> outputBuffer.putFloat(inputBuffer.float * gain)
                    else -> throw IllegalStateException("Unexpected PCM encoding: $encoding")
                }
            }
            readFrames++
        }
    }

    /**
     * Keeps [trackStartFrames] in sync when the decoder advances into the next media
     * item without the audio pipeline flushing (gapless playback).
     */
    private fun syncTrackPosition() {
        if (trackDurationFrames > 0 && readFrames >= trackStartFrames + trackDurationFrames) {
            trackStartFrames += trackDurationFrames
            trackDurationFrames = toFrames(state.trackDurationUs)
        }
    }

    private fun gainAtPosition(positionFrames: Long): Float {
        if (state.fadeDurationUs <= 0L || sampleRate <= 0) return 1.0f

        val posInTrack = (positionFrames - trackStartFrames).coerceAtLeast(0L)
        val totalFrames = if (trackDurationFrames != C.TIME_UNSET) {
            trackDurationFrames.coerceAtLeast(0L)
        } else {
            Long.MAX_VALUE
        }

        var fadeFrames = (state.fadeDurationUs * sampleRate / 1_000_000L).coerceAtLeast(1L)
        fadeFrames = fadeFrames.coerceAtMost(totalFrames / 2)
        if (fadeFrames <= 0L) return 1.0f

        val fadeIn = if (posInTrack >= fadeFrames) {
            1.0f
        } else {
            posInTrack.toFloat() / fadeFrames.toFloat()
        }
        val fadeOut = if (totalFrames == Long.MAX_VALUE) {
            1.0f
        } else {
            val remaining = totalFrames - posInTrack
            if (remaining >= fadeFrames) 1.0f else (remaining.toFloat() / fadeFrames.toFloat())
        }
        return min(fadeIn, fadeOut)
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
