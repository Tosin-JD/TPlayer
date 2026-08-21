package com.tosin.musicplayer.ui.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tosin.musicplayer.data.repository.ThemeDataStoreRepository
import com.tosin.musicplayer.ui.theme.engine.ThemeParameters
import com.tosin.musicplayer.ui.theme.engine.ThemeState
import com.tosin.musicplayer.ui.theme.engine.ThemeStyle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = mockk<ThemeDataStoreRepository>(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun selectStyle_updatesActiveStyleAndPersists() = runTest(dispatcher) {
        coEvery { repository.loadThemeState() } returns ThemeState()

        val viewModel = ThemeViewModel(repository)
        advanceUntilIdle()

        viewModel.selectStyle(ThemeStyle.NEUMORPHISM)
        advanceUntilIdle()

        assertEquals(ThemeStyle.NEUMORPHISM, viewModel.themeState.value.activeStyle)
        coVerify { repository.saveThemeState(match { it.activeStyle == ThemeStyle.NEUMORPHISM }) }
    }

    @Test
    fun updateVisualParameters_updatesActivePresetOnly() = runTest(dispatcher) {
        coEvery { repository.loadThemeState() } returns ThemeState(activeStyle = ThemeStyle.RETRO_MONO)

        val viewModel = ThemeViewModel(repository)
        advanceUntilIdle()

        viewModel.updateBorderStrokeWidth(4.dp)
        viewModel.updateCornerRadiusScale(0.5f)
        viewModel.updateSurfaceBlur(10.dp)
        viewModel.updatePrimaryColor(Color(0xFF00E5FF))
        advanceUntilIdle()

        val currentRetroParams = viewModel.themeState.value.currentParams
        assertEquals(4.dp, currentRetroParams.borderStrokeWidth)
        assertEquals(0.5f, currentRetroParams.cornerRadiusScale)
        assertEquals(10.dp, currentRetroParams.surfaceBlur)
        assertEquals(Color(0xFF00E5FF), currentRetroParams.primaryColor)

        // Verify other styles remain unaffected
        val m3Params = viewModel.themeState.value.styleConfigs[ThemeStyle.MATERIAL_EXPRESSIVE]
        assertEquals(ThemeParameters.materialExpressiveDefault().borderStrokeWidth, m3Params?.borderStrokeWidth)
    }

    @Test
    fun resetCurrentStyleToDefault_restoresFactoryPreset() = runTest(dispatcher) {
        val modifiedCyberpunk = ThemeParameters.cyberpunkDefault().copy(
            glowRadius = 0.dp,
            cornerRadiusScale = 2.0f
        )
        val initialState = ThemeState(
            activeStyle = ThemeStyle.CYBERPUNK,
            styleConfigs = mapOf(ThemeStyle.CYBERPUNK to modifiedCyberpunk)
        )
        coEvery { repository.loadThemeState() } returns initialState

        val viewModel = ThemeViewModel(repository)
        advanceUntilIdle()

        viewModel.resetCurrentStyleToDefault()
        advanceUntilIdle()

        val resetParams = viewModel.themeState.value.currentParams
        assertEquals(ThemeParameters.cyberpunkDefault().glowRadius, resetParams.glowRadius)
        assertEquals(ThemeParameters.cyberpunkDefault().cornerRadiusScale, resetParams.cornerRadiusScale)
    }

    @Test
    fun applyCurrentParamsToAllStyles_copiesConfigurationGlobally() = runTest(dispatcher) {
        coEvery { repository.loadThemeState() } returns ThemeState(activeStyle = ThemeStyle.CLAYMORPHISM)

        val viewModel = ThemeViewModel(repository)
        advanceUntilIdle()

        viewModel.updatePrimaryColor(Color(0xFFFF0055))
        viewModel.applyCurrentParamsToAllStyles()
        advanceUntilIdle()

        ThemeStyle.entries.forEach { style ->
            val config = viewModel.themeState.value.styleConfigs[style]
            assertEquals(Color(0xFFFF0055), config?.primaryColor)
        }
    }

    @Test
    fun brutalism_theme_is_available_and_uses_bold_raw_defaults() = runTest(dispatcher) {
        coEvery { repository.loadThemeState() } returns ThemeState()

        val viewModel = ThemeViewModel(repository)
        advanceUntilIdle()

        viewModel.selectStyle(ThemeStyle.BRUTALISM)
        advanceUntilIdle()

        val brutalism = viewModel.themeState.value.styleConfigs[ThemeStyle.BRUTALISM]
        assertEquals(ThemeStyle.BRUTALISM, viewModel.themeState.value.activeStyle)
        assertEquals(Color(0xFFFF3333), brutalism?.primaryColor)
        assertEquals(0.0f, brutalism?.cornerRadiusScale ?: 1f)
        assertEquals(3.dp, brutalism?.borderStrokeWidth)
        assertEquals(false, brutalism?.useSystemFont)
    }
}
