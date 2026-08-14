package com.tosin.musicplayer.ui.components.menu

import androidx.compose.ui.graphics.vector.ImageVector

data class SelectionOption<T>(
    val value: T,
    val label: String,
    val icon: ImageVector? = null,
    val destructive: Boolean = false
)

data class ActionMenuOption(
    val label: String,
    val icon: ImageVector? = null,
    val destructive: Boolean = false,
    val onClick: () -> Unit
)
