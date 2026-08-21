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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
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
    shape: Shape = RoundedCornerShape(4.dp),
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
                    drawOffsetHardShadow(
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
                    drawOffsetHardShadow(
                        shadowColor = params.shadowColor,
                        elevation = params.shadowElevation,
                        shape = scaledShape
                    )
                }
            }
        }
        ThemeStyle.NEO_BRUTALISM -> {
            if (params.shadowElevation > 0.dp) {
                modifier = modifier.drawBehind {
                    drawOffsetHardShadow(
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

// ── Shape Helpers ──

/**
 * Extracts the corner radius in pixels from a shape.
 * Returns 0f for non-RoundedCornerShape (sharp edges).
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.extractCornerRadiusPx(shape: Shape): Float {
    if (shape is RoundedCornerShape) {
        // Use the topStart radius as a representative value
        return shape.topStart.toPx(size, this)
    }
    return 0f
}

/**
 * Creates a Path from a shape's outline for use in shadow drawing.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.shapeToPath(shape: Shape): Path {
    val path = Path()
    val outline = shape.createOutline(size, layoutDirection, this)
    when (outline) {
        is androidx.compose.ui.graphics.Outline.Rounded -> {
            path.addRoundRect(
                RoundRect(
                    left = 0f,
                    top = 0f,
                    right = size.width,
                    bottom = size.height,
                    cornerRadius = outline.roundRect.topLeftCornerRadius
                )
            )
        }
        is androidx.compose.ui.graphics.Outline.Rectangle -> {
            path.addRect(outline.rect)
        }
        is androidx.compose.ui.graphics.Outline.Generic -> {
            path.addPath(outline.path)
        }
    }
    return path
}

// ── Private Shader & Drawing Routines ──

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawNeonGlow(
    glowColor: Color,
    glowRadius: Dp,
    shape: Shape
) {
    val radiusPx = glowRadius.toPx()
    if (radiusPx <= 0f) return

    val cornerPx = extractCornerRadiusPx(shape)

    drawIntoCanvas { canvas ->
        val paint = Paint().asFrameworkPaint().apply {
            color = glowColor.toArgb()
            isAntiAlias = true
            maskFilter = BlurMaskFilter(radiusPx, BlurMaskFilter.Blur.OUTER)
        }
        canvas.nativeCanvas.drawRoundRect(
            0f, 0f, size.width, size.height,
            cornerPx, cornerPx,
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
    val cornerPx = extractCornerRadiusPx(shape)

    drawIntoCanvas { canvas ->
        // Bottom-Right Dark Shadow
        val darkPaint = Paint().asFrameworkPaint().apply {
            color = shadowColor.toArgb()
            isAntiAlias = true
            maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.nativeCanvas.drawRoundRect(
            offsetPx, offsetPx, size.width + offsetPx, size.height + offsetPx,
            cornerPx, cornerPx,
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
            cornerPx, cornerPx,
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
    val cornerPx = extractCornerRadiusPx(shape)

    drawIntoCanvas { canvas ->
        val paint = Paint().asFrameworkPaint().apply {
            color = shadowColor.toArgb()
            isAntiAlias = true
            maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.nativeCanvas.drawRoundRect(
            0f, offsetPx, size.width, size.height + offsetPx,
            cornerPx, cornerPx,
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
    val cornerPx = extractCornerRadiusPx(shape)

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
            radiusX = cornerPx,
            radiusY = cornerPx,
            paint = paint
        )
    }
}

/**
 * Unified hard offset shadow — respects the shape's corner radius.
 * When cornerRadius = 0 (Brutalism/RetroMono), draws sharp rectangles.
 * When cornerRadius > 0 (Neo-Brutalism), draws rounded rectangles.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawOffsetHardShadow(
    shadowColor: Color,
    elevation: Dp,
    shape: Shape
) {
    val offsetPx = elevation.toPx()
    val cornerPx = extractCornerRadiusPx(shape)

    drawIntoCanvas { canvas ->
        val paint = Paint().apply {
            color = shadowColor
            style = PaintingStyle.Fill
            isAntiAlias = true
        }
        canvas.drawRoundRect(
            left = offsetPx,
            top = offsetPx,
            right = size.width + offsetPx,
            bottom = size.height + offsetPx,
            radiusX = cornerPx,
            radiusY = cornerPx,
            paint = paint
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMaterialElevationShadow(
    shadowColor: Color,
    elevation: Dp,
    shape: Shape
) {
    val offsetPx = elevation.toPx()
    val blurPx = (elevation * 1.2f).toPx()
    if (blurPx <= 0f) return
    val cornerPx = extractCornerRadiusPx(shape)

    drawIntoCanvas { canvas ->
        val paint = Paint().asFrameworkPaint().apply {
            color = shadowColor.toArgb()
            isAntiAlias = true
            maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.nativeCanvas.drawRoundRect(
            0f, offsetPx / 2, size.width, size.height + offsetPx / 2,
            cornerPx, cornerPx,
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
