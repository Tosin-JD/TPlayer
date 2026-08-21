package com.tosin.musicplayer.ui.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tosin.musicplayer.data.repository.ThemeDataStoreRepository
import com.tosin.musicplayer.ui.theme.engine.ThemeParameters
import com.tosin.musicplayer.ui.theme.engine.ThemeState
import com.tosin.musicplayer.ui.theme.engine.ThemeStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ThemeViewModel(
    private val repository: ThemeDataStoreRepository
) : ViewModel() {

    private val _themeState = MutableStateFlow(ThemeState())
    val themeState: StateFlow<ThemeState> = _themeState.asStateFlow()

    init {
        viewModelScope.launch {
            _themeState.value = repository.loadThemeState()
        }
    }

    // ── Style Selection ──
    fun selectStyle(style: ThemeStyle) {
        updateState { it.copy(activeStyle = style) }
    }

    // ── Granular Parameter Mutators ──
    fun updatePrimaryColor(color: Color) = updateActiveParams { it.copy(primaryColor = color) }
    fun updateSecondaryColor(color: Color) = updateActiveParams { it.copy(secondaryColor = color) }
    fun updateBackgroundColor(color: Color) = updateActiveParams { it.copy(backgroundColor = color) }
    fun updateSurfaceColor(color: Color) = updateActiveParams { it.copy(surfaceColor = color) }
    fun updateBorderColor(color: Color) = updateActiveParams { it.copy(borderColor = color) }
    fun updateGlowColor(color: Color) = updateActiveParams { it.copy(glowColor = color) }

    fun updateCornerRadiusScale(scale: Float) = updateActiveParams { it.copy(cornerRadiusScale = scale) }
    fun updateBorderStrokeWidth(width: Dp) = updateActiveParams { it.copy(borderStrokeWidth = width) }
    fun updateSurfaceBlur(blur: Dp) = updateActiveParams { it.copy(surfaceBlur = blur) }
    fun updateSurfaceAlpha(alpha: Float) = updateActiveParams { it.copy(surfaceAlpha = alpha) }
    fun updateShadowElevation(elevation: Dp) = updateActiveParams { it.copy(shadowElevation = elevation) }
    fun updateGlowRadius(glow: Dp) = updateActiveParams { it.copy(glowRadius = glow) }
    fun updateFontScale(scale: Float) = updateActiveParams { it.copy(fontScale = scale) }
    fun toggleSystemFont(useSystem: Boolean) = updateActiveParams { it.copy(useSystemFont = useSystem) }
    fun toggleDarkMode(isDark: Boolean) = updateActiveParams { it.copy(isDark = isDark) }

    // ── Reset & Global Operations ──
    fun resetCurrentStyleToDefault() {
        val currentStyle = _themeState.value.activeStyle
        val defaultParams = when (currentStyle) {
            ThemeStyle.MATERIAL_EXPRESSIVE -> ThemeParameters.materialExpressiveDefault()
            ThemeStyle.CYBERPUNK -> ThemeParameters.cyberpunkDefault()
            ThemeStyle.NEUMORPHISM -> ThemeParameters.neumorphismDefault()
            ThemeStyle.CLAYMORPHISM -> ThemeParameters.claymorphismDefault()
            ThemeStyle.RETRO_MONO -> ThemeParameters.retroMonoDefault()
            ThemeStyle.BRUTALISM -> ThemeParameters.brutalismDefault()
        }
        updateActiveParams { defaultParams }
    }

    fun applyCurrentParamsToAllStyles() {
        val current = _themeState.value.currentParams
        updateState { state ->
            val updated = state.styleConfigs.mapValues { current }
            state.copy(styleConfigs = updated)
        }
    }

    private fun updateActiveParams(transform: (ThemeParameters) -> ThemeParameters) {
        updateState { state ->
            val active = state.activeStyle
            val current = state.styleConfigs[active] ?: ThemeParameters.materialExpressiveDefault()
            val updated = transform(current)
            state.copy(styleConfigs = state.styleConfigs + (active to updated))
        }
    }

    private fun updateState(transform: (ThemeState) -> ThemeState) {
        _themeState.update { current ->
            val updated = transform(current)
            viewModelScope.launch {
                repository.saveThemeState(updated)
            }
            updated
        }
    }
}
