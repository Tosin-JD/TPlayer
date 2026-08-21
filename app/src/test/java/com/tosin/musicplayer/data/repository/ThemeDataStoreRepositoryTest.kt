package com.tosin.musicplayer.data.repository

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tosin.musicplayer.ui.theme.engine.ThemeParameters
import com.tosin.musicplayer.ui.theme.engine.ThemeState
import com.tosin.musicplayer.ui.theme.engine.ThemeStyle
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ThemeDataStoreRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var context: Context
    private lateinit var repository: ThemeDataStoreRepository

    @Before
    fun setUp() {
        context = mockk()
        every { context.filesDir } returns tempFolder.root
        repository = ThemeDataStoreRepository(context)
    }

    @Test
    fun saveAndLoadThemeState_preservesActiveStyleAndCustomOverrides() = runTest {
        val customCyberpunk = ThemeParameters.cyberpunkDefault().copy(
            primaryColor = Color(0xFFFF1177),
            glowRadius = 16.dp,
            cornerRadiusScale = 0.8f,
            surfaceBlur = 12.dp
        )

        val customState = ThemeState(
            activeStyle = ThemeStyle.CYBERPUNK,
            styleConfigs = mapOf(
                ThemeStyle.CYBERPUNK to customCyberpunk,
                ThemeStyle.MATERIAL_EXPRESSIVE to ThemeParameters.materialExpressiveDefault()
            )
        )

        repository.saveThemeState(customState)

        val loaded = repository.loadThemeState()
        assertEquals(ThemeStyle.CYBERPUNK, loaded.activeStyle)

        val loadedCyberpunk = loaded.styleConfigs[ThemeStyle.CYBERPUNK]
        assertEquals(Color(0xFFFF1177), loadedCyberpunk?.primaryColor)
        assertEquals(16.dp, loadedCyberpunk?.glowRadius)
        assertEquals(0.8f, loadedCyberpunk?.cornerRadiusScale)
        assertEquals(12.dp, loadedCyberpunk?.surfaceBlur)
    }

    @Test
    fun loadThemeState_whenFileDoesNotExist_returnsDefaultState() = runTest {
        val loaded = repository.loadThemeState()
        assertEquals(ThemeStyle.MATERIAL_EXPRESSIVE, loaded.activeStyle)
        assertEquals(ThemeStyle.entries.size, loaded.styleConfigs.size)
    }
}
