package com.tosin.musicplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tosin.musicplayer.ui.theme.AppSpacing
import com.tosin.musicplayer.ui.theme.engine.ThemeStyle
import com.tosin.musicplayer.ui.theme.engine.customAppSurface
import com.tosin.musicplayer.ui.viewmodel.SettingsViewModel
import com.tosin.musicplayer.ui.viewmodel.ThemeViewModel
import com.tosin.musicplayer.ui.icons.AppIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeStudioScreen(
    viewModel: ThemeViewModel,
    settingsViewModel: SettingsViewModel? = null,
    onNavigateBack: () -> Unit
) {
    val themeState by viewModel.themeState.collectAsState()
    val params = themeState.currentParams
    val activeStyle = themeState.activeStyle

    var showResetDialog by remember { mutableStateOf(false) }
    var showApplyAllDialog by remember { mutableStateOf(false) }

    val settingsUiState = settingsViewModel?.let { vm ->
        val state by vm.uiState.collectAsState()
        state
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Theme Studio", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(AppIcons.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(AppIcons.RestartAlt, contentDescription = "Reset Style")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ── 1. Live Interactive Sandbox Container ──
            item {
                Text(
                    text = "LIVE INTERACTIVE PREVIEW",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                LiveThemePreviewSandbox(
                    activeStyle = activeStyle,
                    onPrimaryClick = {}
                )
            }

            // ── 2. Base UI Style Selector ──
            item {
                Text(
                    text = "BASE DESIGN PARADIGM",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ThemeStyle.entries.forEach { style ->
                        val isSelected = style == activeStyle
                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .customAppSurface(
                                    shape = RoundedCornerShape(4.dp),
                                    backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    onClick = { viewModel.selectStyle(style) }
                                )
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = style.displayName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = style.name.lowercase(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // ── 3. Color Customization Section ──
            item {
                StudioSectionCard(title = "Color Palette Overrides") {
                    ColorPickerRow(
                        label = "Accent / Primary",
                        currentColor = params.primaryColor,
                        onColorSelected = { viewModel.updatePrimaryColor(it) }
                    )
                    Spacer(Modifier.height(12.dp))
                    ColorPickerRow(
                        label = "Secondary Color",
                        currentColor = params.secondaryColor,
                        onColorSelected = { viewModel.updateSecondaryColor(it) }
                    )
                    Spacer(Modifier.height(12.dp))
                    ColorPickerRow(
                        label = "Surface / Card",
                        currentColor = params.surfaceColor,
                        onColorSelected = { viewModel.updateSurfaceColor(it) }
                    )
                }
            }

            // ── 4. Geometry & Corners Section ──
            item {
                StudioSectionCard(title = "Geometry & Shapes") {
                    Text(
                        text = "Corner Roundness: ${String.format("%.1fx", params.cornerRadiusScale)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = params.cornerRadiusScale,
                        onValueChange = { viewModel.updateCornerRadiusScale(it) },
                        valueRange = 0.0f..2.0f,
                        steps = 19
                    )

                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Border Stroke Width: ${params.borderStrokeWidth.value.toInt()} dp",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = params.borderStrokeWidth.value,
                        onValueChange = { viewModel.updateBorderStrokeWidth(it.dp) },
                        valueRange = 0f..4f,
                        steps = 7
                    )
                }
            }

            // ── 5. Lighting, Shadows & Special Effects ──
            item {
                StudioSectionCard(title = "Optics, Shadows & FX") {
                    Text(
                        text = "Surface Glass Blur: ${params.surfaceBlur.value.toInt()} dp",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = params.surfaceBlur.value,
                        onValueChange = { viewModel.updateSurfaceBlur(it.dp) },
                        valueRange = 0f..24f,
                        steps = 11
                    )

                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Shadow / Elevation Depth: ${params.shadowElevation.value.toInt()} dp",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = params.shadowElevation.value,
                        onValueChange = { viewModel.updateShadowElevation(it.dp) },
                        valueRange = 0f..16f,
                        steps = 15
                    )

                    if (activeStyle == ThemeStyle.CYBERPUNK) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Neon Glow Intensity: ${params.glowRadius.value.toInt()} dp",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Slider(
                            value = params.glowRadius.value,
                            onValueChange = { viewModel.updateGlowRadius(it.dp) },
                            valueRange = 0f..20f,
                            steps = 9
                        )
                    }
                }
            }

            // ── 6. Typography Scale ──
            item {
                StudioSectionCard(title = "Typography") {
                    Text(
                        text = "Font Scale: ${String.format("%.2fx", params.fontScale)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = params.fontScale,
                        onValueChange = { viewModel.updateFontScale(it) },
                        valueRange = 0.8f..1.4f,
                        steps = 5
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Use System Default Font", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = params.useSystemFont,
                            onCheckedChange = { viewModel.toggleSystemFont(it) }
                        )
                    }
                }
            }

            // ── 7. Layout (Home Screen Tabs) ──
            if (settingsViewModel != null && settingsUiState != null) {
                item {
                    StudioSectionCard(title = "Home Screen Tabs") {
                        Text(
                            text = "Reorder and toggle tabs on the home screen",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        settingsUiState!!.tabOrder.forEachIndexed { index, tab ->
                            val isChecked = settingsUiState!!.visibleTabs.any { it.equals(tab, ignoreCase = true) }
                            val isLastVisible = isChecked && settingsUiState!!.visibleTabs.size == 1
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    enabled = !isLastVisible,
                                    onCheckedChange = { settingsViewModel.toggleTabVisibility(tab) }
                                )
                                Text(
                                    text = tab,
                                    modifier = Modifier.weight(1f),
                                    color = if (isChecked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                )
                                IconButton(
                                    onClick = { if (index > 0) settingsViewModel.reorderTab(index, index - 1) },
                                    enabled = index > 0
                                ) {
                                    Icon(AppIcons.ArrowUp, contentDescription = "Move Up")
                                }
                                IconButton(
                                    onClick = { if (index < settingsUiState!!.tabOrder.size - 1) settingsViewModel.reorderTab(index, index + 1) },
                                    enabled = index < settingsUiState!!.tabOrder.size - 1
                                ) {
                                    Icon(AppIcons.ArrowDown, contentDescription = "Move Down")
                                }
                            }
                        }
                    }
                }
            }

            // ── 8. Global Actions ──
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reset Preset")
                    }
                    Button(
                        onClick = { showApplyAllDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Apply to All")
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    // ── Dialogs ──
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset ${activeStyle.displayName}?") },
            text = { Text("This will revert all visual overrides for this style back to its factory defaults.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetCurrentStyleToDefault()
                    showResetDialog = false
                }) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showApplyAllDialog) {
        AlertDialog(
            onDismissRequest = { showApplyAllDialog = false },
            title = { Text("Apply Globally?") },
            text = { Text("Copy the current color, blur, and corner configurations to all other style presets.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.applyCurrentParamsToAllStyles()
                    showApplyAllDialog = false
                }) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApplyAllDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun LiveThemePreviewSandbox(
    activeStyle: ThemeStyle,
    onPrimaryClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .customAppSurface(
                shape = RoundedCornerShape(4.dp)
            )
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Simulated Mini Player Component
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .customAppSurface(
                            shape = RoundedCornerShape(4.dp),
                            backgroundColor = MaterialTheme.colorScheme.primary
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        AppIcons.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Synthwave Odyssey",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "TPlayer Studio Live",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onPrimaryClick) {
                    Icon(AppIcons.Play, contentDescription = "Play")
                }
            }

            // Visualizer simulation bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                val heights = listOf(14.dp, 24.dp, 10.dp, 28.dp, 18.dp, 22.dp, 8.dp, 26.dp, 16.dp)
                heights.forEach { h ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(h)
                            .customAppSurface(
                                shape = RoundedCornerShape(4.dp),
                                backgroundColor = MaterialTheme.colorScheme.primary
                            )
                    )
                }
            }

            // Component showcase: Button, Chip, Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = {}) {
                    Text("Action")
                }
                SuggestionChip(
                    onClick = {},
                    label = { Text(activeStyle.displayName) }
                )
                var switchState by remember { mutableStateOf(true) }
                Switch(
                    checked = switchState,
                    onCheckedChange = { switchState = it }
                )
            }
        }
    }
}

@Composable
private fun StudioSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .customAppSurface(
                shape = MaterialTheme.shapes.medium,
                backgroundColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            content()
        }
    }
}

@Composable
private fun ColorPickerRow(
    label: String,
    currentColor: Color,
    onColorSelected: (Color) -> Unit
) {
    val samplePalette = listOf(
        Color(0xFF6750A4), Color(0xFFFF007F), Color(0xFF00F0FF),
        Color(0xFF39FF14), Color(0xFFFF9800), Color(0xFF2196F3),
        Color(0xFFE91E63), Color(0xFF000000), Color(0xFFFFFFFF)
    )

    Column {
        Text(text = label, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            samplePalette.forEach { color ->
                val isSelected = color == currentColor
                Box(
                        modifier = Modifier
                            .size(32.dp)
                            .customAppSurface(
                                shape = MaterialTheme.shapes.small,
                                backgroundColor = color,
                                onClick = { onColorSelected(color) }
                            )
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Gray.copy(alpha = 0.5f),
                                shape = MaterialTheme.shapes.small
                            ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            AppIcons.Check,
                            contentDescription = null,
                            tint = if (color == Color.White) Color.Black else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
