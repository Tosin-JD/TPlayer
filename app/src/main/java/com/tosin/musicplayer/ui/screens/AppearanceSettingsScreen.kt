package com.tosin.musicplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tosin.musicplayer.ui.theme.AccentColors
import com.tosin.musicplayer.ui.theme.AppThemePreset
import com.tosin.musicplayer.ui.theme.AppSpacing
import com.tosin.musicplayer.ui.theme.engine.ThemeStyle
import com.tosin.musicplayer.ui.viewmodel.SettingsViewModel
import com.tosin.musicplayer.ui.viewmodel.ThemeViewModel
import com.tosin.musicplayer.ui.icons.AppIcons

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppearanceSettingsScreen(
    viewModel: SettingsViewModel,
    themeViewModel: ThemeViewModel? = null,
    onNavigateBack: () -> Unit,
    onNavigateToThemeStudio: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appearance", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(AppIcons.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Multi-Style Theme Studio ──
            SettingsSubHeader("Customization Engine")

            ListItem(
                headlineContent = { Text("Theme Studio", fontWeight = FontWeight.SemiBold) },
                supportingContent = { Text("Customize blur, shapes, shadows, glows & styles") },
                leadingContent = {
                    Icon(AppIcons.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingContent = {
                    Icon(AppIcons.ArrowForward, contentDescription = "Open Studio")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToThemeStudio)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = AppSpacing.xSmall))

            // ── Design Paradigm ──
            if (themeViewModel != null) {
                val themeState by themeViewModel.themeState.collectAsState()
                Text(
                    text = "Design Paradigm",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = AppSpacing.large, vertical = AppSpacing.small)
                )
                Text(
                    text = "Choose a base visual style. Each paradigm changes shapes, shadows, borders, and typography.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = AppSpacing.large)
                )
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.large, vertical = AppSpacing.medium),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
                ) {
                    ThemeStyle.entries.forEach { style ->
                        val isSelected = style == themeState.activeStyle
                        FilterChip(
                            selected = isSelected,
                            onClick = { themeViewModel.selectStyle(style) },
                            label = { Text(style.displayName) }
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = AppSpacing.xSmall))
            }

            // ── Theme ──
            SettingsSubHeader("Theme")

            ListItem(
                headlineContent = { Text("Dark Mode") },
                supportingContent = { Text("Adjust the app theme for low light") },
                leadingContent = {
                    Icon(AppIcons.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingContent = {
                    Switch(
                        checked = uiState.isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode(it) }
                    )
                }
            )

            ListItem(
                headlineContent = { Text("Dynamic Color") },
                supportingContent = { Text("Use colors from your wallpaper (Android 12+)") },
                leadingContent = {
                    Icon(AppIcons.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingContent = {
                    Switch(
                        checked = uiState.useDynamicColor,
                        onCheckedChange = { viewModel.toggleDynamicColor(it) }
                    )
                }
            )

            Text(
                text = "Theme Preset",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = AppSpacing.large, vertical = AppSpacing.small)
            )

            Text(
                text = "Choose a visual mood. AMOLED is pure black, while the others create stronger personalities for the app.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = AppSpacing.large)
            )

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.large, vertical = AppSpacing.medium),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
            ) {
                AppThemePreset.entries.forEach { preset ->
                    FilterChip(
                        selected = uiState.themePreset.equals(preset.label, ignoreCase = true),
                        onClick = { viewModel.setThemePreset(preset.label) },
                        label = { Text(preset.label) }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = AppSpacing.xSmall))

            // ── Accent Color ──
            Text(
                text = "Accent Color",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = AppSpacing.large, vertical = AppSpacing.small)
            )

            Text(
                text = "Pick a primary accent used across the app.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = AppSpacing.large)
            )

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.large, vertical = AppSpacing.medium),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
            ) {
                AccentColors.forEachIndexed { index, color ->
                    val selected = uiState.accentColorIndex == index
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(color)
                            .border(
                                width = if (selected) 3.dp else 1.dp,
                                color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                                shape = MaterialTheme.shapes.medium
                            )
                            .clickable { viewModel.setAccentColor(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selected) {
                            Icon(
                                AppIcons.Check,
                                contentDescription = null,
                                tint = if (color.luminance() > 0.5f) Color.Black else Color.White
                            )
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = AppSpacing.xSmall))

            // ── Layout ──
            SettingsSubHeader("Layout")

            // Reorder tabs
            ListItem(
                headlineContent = { Text("Home Screen Tabs") },
                supportingContent = { Text("Reorder and toggle tabs on the home screen") },
                leadingContent = {
                    Icon(AppIcons.ViewList, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            )

            Column(modifier = Modifier.padding(horizontal = AppSpacing.large)) {
                uiState.tabOrder.forEachIndexed { index, tab ->
                    val isChecked = uiState.visibleTabs.any { it.equals(tab, ignoreCase = true) }
                    val isLastVisible = isChecked && uiState.visibleTabs.size == 1
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = isChecked,
                            enabled = !isLastVisible,
                            onCheckedChange = { viewModel.toggleTabVisibility(tab) }
                        )
                        Text(
                            text = tab,
                            modifier = Modifier.weight(1f),
                            color = if (isChecked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                        IconButton(
                            onClick = { if (index > 0) viewModel.reorderTab(index, index - 1) },
                            enabled = index > 0
                        ) {
                            Icon(AppIcons.ArrowUp, contentDescription = "Move Up")
                        }
                        IconButton(
                            onClick = { if (index < uiState.tabOrder.size - 1) viewModel.reorderTab(index, index + 1) },
                            enabled = index < uiState.tabOrder.size - 1
                        ) {
                            Icon(AppIcons.ArrowDown, contentDescription = "Move Down")
                        }
                    }
                }
            }


            Spacer(Modifier.weight(1f))

            // Reset Appearance Settings
            ResetSettingsButton(
                label = "Reset Appearance",
                onClick = { showResetDialog = true }
            )

            Spacer(Modifier.height(AppSpacing.xLarge))
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = { Icon(AppIcons.RestartAlt, contentDescription = null) },
            title = { Text("Reset Appearance?") },
            text = { Text("This will reset your theme, colors, and tab layouts to defaults.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetAppearanceSettings()
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
}
