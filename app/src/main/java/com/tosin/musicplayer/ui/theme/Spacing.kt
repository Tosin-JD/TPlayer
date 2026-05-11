package com.tosin.musicplayer.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object AppSpacing {
    val xSmall = 4.dp
    val small = 8.dp
    val medium = 12.dp
    val large = 16.dp
    val xLarge = 24.dp
    val xxLarge = 32.dp

    val screenHorizontal = large
    val itemSpacing = medium
    val sectionSpacing = xLarge
    val cardPadding = large
}

fun standardScreenPadding(
    top: Dp = AppSpacing.medium,
    bottom: Dp = AppSpacing.xLarge
): PaddingValues = PaddingValues(
    start = AppSpacing.screenHorizontal,
    end = AppSpacing.screenHorizontal,
    top = top,
    bottom = bottom
)
