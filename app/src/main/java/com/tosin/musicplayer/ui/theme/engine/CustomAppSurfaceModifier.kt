package com.tosin.musicplayer.ui.theme.engine

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Universal Surface modifier that evaluates active ThemeParameters and ThemeStyle
 * to render style-authentic lighting, shadows, borders, glows, and blur.
 */
fun Modifier.customAppSurface(
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color? = null,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    val params = AppTheme.parameters
    val style = AppTheme.style

    val effectiveBgColor = (backgroundColor ?: params.surfaceColor).copy(alpha = params.surfaceAlpha)
    val scaledShape = scaleShape(shape, params.cornerRadiusScale)

    var modifier: Modifier = Modifier

    // ── 1. Apply Outer Effects (Glows & Drop Shadows before clipping) ──
    when (style) {
        ThemeStyle.CYBERPUNK -> {
            if (params.glowRadius > 0.dp) {
                modifier = modifier.drawBehind {
                    drawNeonGlow(
                        glowColor = if (params.glowColor != Color.Transparent) params.glowColor else params.primaryColor.copy(alpha = 0.5f),
                        glowRadius = params.glowRadius,
                        shape = scaledShape
                    )
                }
            }
        }
        ThemeStyle.NEUMORPHISM -> {
            modifier = modifier.drawBehind {
                drawNeumorphicOuterShadows(
                    shadowColor = params.shadowColor,
                    highlightColor = params.highlightColor,
                    elevation = params.shadowElevation,
                    shape = scaledShape
                )
            }
        }
        ThemeStyle.CLAYMORPHISM -> {
            modifier = modifier.drawBehind {
                drawClaymorphicDropShadow(
                    shadowColor = params.shadowColor,
                    elevation = params.shadowElevation,
                    shape = scaledShape
                )
            }
        }
        ThemeStyle.RETRO_MONO -> {
            if (params.shadowElevation > 0.dp) {
                modifier = modifier.drawBehind {
                    drawRetroHardShadow(
                        shadowColor = params.shadowColor,
                        elevation = params.shadowElevation,
                        shape = scaledShape
                    )
                }
            }
        }
        ThemeStyle.BRUTALISM -> {
            if (params.shadowElevation > 0.dp) {
                modifier = modifier.drawBehind {
                    drawBrutalistHardShadow(
                        shadowColor = params.shadowColor,
                        elevation = params.shadowElevation,
                        shape = scaledShape
                    )
                }
            }
        }
        ThemeStyle.MATERIAL_EXPRESSIVE -> {
            if (params.shadowElevation > 0.dp) {
                modifier = modifier.drawBehind {
                    drawMaterialElevationShadow(
                        shadowColor = params.shadowColor,
                        elevation = params.shadowElevation,
                        shape = scaledShape
                    )
                }
            }
        }
    }

    // ── 2. Glassmorphism Blur ──
    if (params.surfaceBlur > 0.dp) {
        modifier = modifier.blur(params.surfaceBlur)
    }

    // ── 3. Clip & Background Surface Fill ──
    modifier = modifier
        .clip(scaledShape)
        .background(color = effectiveBgColor, shape = scaledShape)

    // ── 4. Inner Highlights (Claymorphism 3D Rim / Neumorphic Insets) ──
    if (style == ThemeStyle.CLAYMORPHISM) {
        modifier = modifier.drawWithContent {
            drawContent()
            drawClayInnerHighlight(
                highlightColor = params.highlightColor,
                strokeWidth = params.borderStrokeWidth.coerceAtLeast(1.5.dp),
                shape = scaledShape
            )
        }
    }

    // ── 5. Border Strokes ──
    if (params.borderStrokeWidth > 0.dp) {
        val effectiveBorderColor = if (params.borderColor != Color.Transparent) {
            params.borderColor
        } else {
            params.primaryColor.copy(alpha = 0.5f)
        }
        modifier = modifier.border(
            width = params.borderStrokeWidth,
            color = effectiveBorderColor,
            shape = scaledShape
        )
    }

    // ── 6. Click Handling ──
    if (onClick != null) {
        modifier = modifier.clickable(onClick = onClick)
    }

    modifier
}

// ── Private Shader & Drawing Routines ──

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawNeonGlow(
    glowColor: Color,
    glowRadius: Dp,
    shape: Shape
) {
    val radiusPx = glowRadius.toPx()
    if (radiusPx <= 0f) return

    drawIntoCanvas { canvas ->
        val paint = Paint().asFrameworkPaint().apply {
            color = glowColor.toArgb()
            isAntiAlias = true
            maskFilter = BlurMaskFilter(radiusPx, BlurMaskFilter.Blur.OUTER)
        }
        canvas.nativeCanvas.drawRoundRect(
            0f, 0f, size.width, size.height,
            16f, 16f,
            paint
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawNeumorphicOuterShadows(
    shadowColor: Color,
    highlightColor: Color,
    elevation: Dp,
    shape: Shape
) {
    val offsetPx = elevation.toPx()
    val blurPx = (elevation * 1.5f).toPx().coerceAtLeast(1f)

    drawIntoCanvas { canvas ->
        // Bottom-Right Dark Shadow
        val darkPaint = Paint().asFrameworkPaint().apply {
            color = shadowColor.toArgb()
            isAntiAlias = true
            maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.nativeCanvas.drawRoundRect(
            offsetPx, offsetPx, size.width + offsetPx, size.height + offsetPx,
            24f, 24f,
            darkPaint
        )

        // Top-Left Light Highlight
        val lightPaint = Paint().asFrameworkPaint().apply {
            color = highlightColor.toArgb()
            isAntiAlias = true
            maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.nativeCanvas.drawRoundRect(
            -offsetPx, -offsetPx, size.width - offsetPx, size.height - offsetPx,
            24f, 24f,
            lightPaint
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawClaymorphicDropShadow(
    shadowColor: Color,
    elevation: Dp,
    shape: Shape
) {
    val offsetPx = elevation.toPx()
    val blurPx = (elevation * 1.8f).toPx().coerceAtLeast(1f)

    drawIntoCanvas { canvas ->
        val paint = Paint().asFrameworkPaint().apply {
            color = shadowColor.toArgb()
            isAntiAlias = true
            maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.nativeCanvas.drawRoundRect(
            0f, offsetPx, size.width, size.height + offsetPx,
            32f, 32f,
            paint
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawClayInnerHighlight(
    highlightColor: Color,
    strokeWidth: Dp,
    shape: Shape
) {
    val strokePx = strokeWidth.toPx()
    drawIntoCanvas { canvas ->
        val paint = Paint().apply {
            color = highlightColor
            style = PaintingStyle.Stroke
            this.strokeWidth = strokePx
            isAntiAlias = true
        }
        canvas.drawRoundRect(
            left = strokePx / 2,
            top = strokePx / 2,
            right = size.width - strokePx / 2,
            bottom = size.height - strokePx / 2,
            radiusX = 32f,
            radiusY = 32f,
            paint = paint
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRetroHardShadow(
    shadowColor: Color,
    elevation: Dp,
    shape: Shape
) {
    val offsetPx = elevation.toPx()
    // Solid, unblurred offset box
    drawRect(
        color = shadowColor,
        topLeft = Offset(offsetPx, offsetPx),
        size = size
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBrutalistHardShadow(
    shadowColor: Color,
    elevation: Dp,
    shape: Shape
) {
    val offsetPx = elevation.toPx()
    drawRect(
        color = shadowColor,
        topLeft = Offset(offsetPx, offsetPx),
        size = size
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMaterialElevationShadow(
    shadowColor: Color,
    elevation: Dp,
    shape: Shape
) {
    val offsetPx = elevation.toPx()
    val blurPx = (elevation * 1.2f).toPx()
    if (blurPx <= 0f) return

    drawIntoCanvas { canvas ->
        val paint = Paint().asFrameworkPaint().apply {
            color = shadowColor.toArgb()
            isAntiAlias = true
            maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.nativeCanvas.drawRoundRect(
            0f, offsetPx / 2, size.width, size.height + offsetPx / 2,
            24f, 24f,
            paint
        )
    }
}

private fun scaleShape(shape: Shape, multiplier: Float): Shape {
    if (shape !is RoundedCornerShape) return shape
    return RoundedCornerShape(
        topStart = 16.dp * multiplier,
        topEnd = 16.dp * multiplier,
        bottomEnd = 16.dp * multiplier,
        bottomStart = 16.dp * multiplier
    )
}
