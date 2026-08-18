package com.tosin.musicplayer.ui.visualizer.styles

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.tosin.musicplayer.ui.visualizer.FftProcessor
import com.tosin.musicplayer.ui.visualizer.VisualizationStyle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Heartbeat Karaoke Rhythm Visualizer:
 * Static radial positions with dramatic inside-to-outside bouncing following song rhythm.
 *
 * - ZERO ROTATION: Orientation is fixed; bars do not spin or rotate around the center.
 * - HEARTBEAT PULSE: Center ring expands and contracts radially (inside <-> outside) on bass beat hits.
 * - INSIDE-TO-OUTSIDE BOUNCE: Bars bounce along their fixed radial axes, extending inward toward center
 *   and outward into space directly driven by FFT magnitudes and song rhythm.
 * - NEON PALETTE: Alternating Neon Cyan (#00E5FF) and Neon Magenta (#FF007F).
 */
class RadialVisualizationStyle : VisualizationStyle {

    override val displayName: String = "Heartbeat Rhythm Burst"

    private companion object {
        const val BASE_RADIUS_FRACTION = 0.16f
        const val MAX_OUTER_LINE_FRACTION = 0.38f
        const val MAX_INNER_LINE_FRACTION = 0.10f
        const val BASE_LINE_WIDTH = 5.5f
        const val HEARTBEAT_EXPANSION_FACTOR = 0.40f

        val NEON_CYAN = Color(0xFF00E5FF)
        val NEON_MAGENTA = Color(0xFFFF007F)
        val DARK_BG_CENTER = Color(0xFF0B061A)
    }

    override fun DrawScope.render(
        magnitudes: FloatArray,
        isPlaying: Boolean,
        primaryColor: Color,
        accentColor: Color,
        timeMs: Long
    ) {
        val barCount = magnitudes.size
        if (barCount == 0) return

        val cx = size.width / 2f
        val cy = size.height / 2f
        val minDim = size.minDimension

        // 1. Extract Bass / Rhythm Beat Energy
        val beatEnergy = if (isPlaying) FftProcessor.extractBeatEnergy(magnitudes) else 0f

        // 2. Beating Heart Radial Expansion (Pulsing inside to outside)
        val ringPulse = beatEnergy * HEARTBEAT_EXPANSION_FACTOR
        val ringRadius = minDim * BASE_RADIUS_FRACTION * (1f + ringPulse)
        val maxOuterLength = minDim * MAX_OUTER_LINE_FRACTION
        val maxInnerLength = minDim * MAX_INNER_LINE_FRACTION

        val angleStep = (2.0 * PI / barCount).toFloat()

        // Background Glow & Concentric Ring
        drawBackgroundAmbient(cx, cy, ringRadius, beatEnergy, isPlaying)
        drawCentralRing(cx, cy, ringRadius, beatEnergy)

        // 3. Render Pure Inside-to-Outside Bouncing Bars (Static Angles, Zero Rotation)
        for (i in 0 until barCount) {
            // Fixed angle for each bar around the circle - ZERO ROTATION
            val angle = angleStep * i - (PI / 2f).toFloat()
            val mag = magnitudes[i].coerceIn(0f, 1f)

            // Direct radial expansion lengths (inside to outside)
            val outerLength = mag * maxOuterLength
            val innerLength = mag * maxInnerLength

            val cosA = cos(angle)
            val sinA = sin(angle)

            // Start inside the ring, end outside the ring
            val startX = cx + cosA * (ringRadius - innerLength)
            val startY = cy + sinA * (ringRadius - innerLength)
            val endX = cx + cosA * (ringRadius + outerLength)
            val endY = cy + sinA * (ringRadius + outerLength)

            // Alternating Neon Cyan & Neon Magenta
            val barColor = if (i % 2 == 0) {
                NEON_CYAN.copy(alpha = 0.85f + mag * 0.15f)
            } else {
                NEON_MAGENTA.copy(alpha = 0.85f + mag * 0.15f)
            }

            val strokeWidth = BASE_LINE_WIDTH + mag * 4.5f

            // Glow shadow line behind spike
            drawLine(
                color = barColor.copy(alpha = 0.35f),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = strokeWidth + 6f,
                cap = StrokeCap.Round
            )

            // Main neon bar line
            drawLine(
                color = barColor,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            // White highlight core on loud bounce hits
            if (mag > 0.40f) {
                val coreStartX = cx + cosA * ringRadius
                val coreStartY = cy + sinA * ringRadius
                drawLine(
                    color = Color.White.copy(alpha = (mag - 0.40f) * 1.4f),
                    start = Offset(coreStartX, coreStartY),
                    end = Offset(endX, endY),
                    strokeWidth = strokeWidth * 0.4f,
                    cap = StrokeCap.Round
                )
            }
        }

        // Inner Core Pulsing Inside to Outside
        drawInnerCore(cx, cy, ringRadius, beatEnergy)
    }

    private fun DrawScope.drawBackgroundAmbient(
        cx: Float, cy: Float, ringRadius: Float, beatEnergy: Float, isPlaying: Boolean
    ) {
        val glowAlpha = if (isPlaying) 0.22f + beatEnergy * 0.28f else 0.08f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    NEON_MAGENTA.copy(alpha = glowAlpha),
                    NEON_CYAN.copy(alpha = glowAlpha * 0.5f),
                    Color.Transparent
                )
            ),
            radius = ringRadius * 2.5f,
            center = Offset(cx, cy)
        )
    }

    private fun DrawScope.drawCentralRing(
        cx: Float, cy: Float, ringRadius: Float, beatEnergy: Float
    ) {
        drawCircle(
            color = NEON_CYAN.copy(alpha = 0.45f + beatEnergy * 0.35f),
            radius = ringRadius,
            center = Offset(cx, cy),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f)
        )
        drawCircle(
            color = NEON_MAGENTA.copy(alpha = 0.35f + beatEnergy * 0.25f),
            radius = ringRadius * 0.85f,
            center = Offset(cx, cy),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
        )
    }

    private fun DrawScope.drawInnerCore(
        cx: Float, cy: Float, ringRadius: Float, beatEnergy: Float
    ) {
        val coreRadius = ringRadius * (0.35f + beatEnergy * 0.25f)
        drawCircle(
            color = DARK_BG_CENTER,
            radius = ringRadius * 0.84f,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = NEON_MAGENTA.copy(alpha = 0.3f + beatEnergy * 0.5f),
            radius = coreRadius,
            center = Offset(cx, cy)
        )
    }
}
