package com.tosin.musicplayer.player

import androidx.media3.common.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * Manages dual-player equal-power audio crossfading between tracks.
 */
class CrossfadeManager(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    var enabled: Boolean = false
    var configuredDurationMs: Long = 3000L // Default 3s (range 0 to 12s)

    private var crossfadeJob: Job? = null
    var isCrossfading: Boolean = false
        private set

    /**
     * Computes Equal-Power crossfade volumes (V_out, V_in) for normalized progress t in [0.0, 1.0].
     * V_out = cos(pi / 2 * t)
     * V_in = sin(pi / 2 * t)
     */
    fun calculateEqualPowerVolumes(progress: Float): Pair<Float, Float> {
        val t = progress.coerceIn(0.0f, 1.0f)
        val angle = (Math.PI / 2.0) * t.toDouble()
        val vOut = cos(angle).toFloat().coerceIn(0.0f, 1.0f)
        val vIn = sin(angle).toFloat().coerceIn(0.0f, 1.0f)
        return Pair(vOut, vIn)
    }

    /**
     * Calculates the effective crossfade duration in milliseconds.
     * Enforces the dynamic safety cap: Track Duration / 2.
     */
    fun getEffectiveDurationMs(trackDurationMs: Long): Long {
        if (trackDurationMs <= 0L) return 0L
        val maxFadeMs = trackDurationMs / 2L
        return minOf(configuredDurationMs, maxFadeMs).coerceAtLeast(0L)
    }

    /**
     * Checks if a crossfade should be triggered given current playback position and duration.
     */
    fun shouldTriggerCrossfade(currentPositionMs: Long, trackDurationMs: Long): Boolean {
        if (!enabled || isCrossfading) return false
        val effectiveMs = getEffectiveDurationMs(trackDurationMs)
        if (effectiveMs <= 0L) return false
        val remainingMs = trackDurationMs - currentPositionMs
        return remainingMs in 1L..effectiveMs
    }

    /**
     * Starts an equal-power crossfade transition between [activePlayer] and [incomingPlayer].
     *
     * @param activePlayer Player fading out.
     * @param incomingPlayer Player fading in.
     * @param durationMs Duration of the crossfade in milliseconds.
     * @param tickIntervalMs Step interval for volume updates (default 20ms).
     * @param onComplete Callback invoked when crossfade finishes.
     */
    fun startCrossfade(
        activePlayer: Player,
        incomingPlayer: Player,
        durationMs: Long,
        tickIntervalMs: Long = 20L,
        onComplete: (() -> Unit)? = null
    ) {
        cancelCrossfade(resetActiveVolume = false)

        if (durationMs <= 0L) {
            activePlayer.volume = 0.0f
            if (activePlayer.isPlaying) {
                activePlayer.pause()
            }
            incomingPlayer.volume = 1.0f
            onComplete?.invoke()
            return
        }

        isCrossfading = true
        activePlayer.volume = 1.0f
        incomingPlayer.volume = 0.0f
        if (!incomingPlayer.isPlaying) {
            incomingPlayer.play()
        }

        crossfadeJob = scope.launch {
            val totalTicks = if (tickIntervalMs > 0L) (durationMs / tickIntervalMs).coerceAtLeast(1L) else 1L
            var tick = 0L

            while (tick <= totalTicks && isCrossfading) {
                val t = (tick.toFloat() / totalTicks.toFloat()).coerceIn(0.0f, 1.0f)
                val (vOut, vIn) = calculateEqualPowerVolumes(t)
                activePlayer.volume = vOut
                incomingPlayer.volume = vIn

                tick++
                if (tick <= totalTicks) {
                    delay(tickIntervalMs)
                }
            }

            if (isCrossfading) {
                // Finalize crossfade
                activePlayer.volume = 0.0f
                if (activePlayer.isPlaying) {
                    activePlayer.pause()
                }
                incomingPlayer.volume = 1.0f
                isCrossfading = false
                onComplete?.invoke()
            }
        }
    }

    /**
     * Cancels any running crossfade immediately, stopping the fading-out player
     * and restoring full volume on the active player.
     */
    fun cancelCrossfade(
        activePlayer: Player? = null,
        fadingOutPlayer: Player? = null,
        resetActiveVolume: Boolean = true
    ) {
        crossfadeJob?.cancel()
        crossfadeJob = null
        isCrossfading = false

        fadingOutPlayer?.let { player ->
            player.volume = 0.0f
            if (player.isPlaying) {
                player.pause()
            }
        }

        if (resetActiveVolume) {
            activePlayer?.volume = 1.0f
        }
    }
}
