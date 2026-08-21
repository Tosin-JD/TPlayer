package com.tosin.musicplayer.ui.viewmodel

import com.tosin.musicplayer.data.repository.MusicRepository
import com.tosin.musicplayer.data.repository.PreferencesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
class SettingsViewModelTabTest {

    private val dispatcher = StandardTestDispatcher()
    private val preferencesRepository = mockk<PreferencesRepository>(relaxed = true)
    private val musicRepository = mockk<MusicRepository>(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { musicRepository.getSongs() } returns flowOf(emptyList())
        coEvery { musicRepository.loadAllSongs() } returns emptyList()
        coEvery { preferencesRepository.loadTabSortOptions() } returns emptyMap()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadSavedSettings_restoresArrangedAndHiddenTabs() = runTest(dispatcher) {
        val savedSettings = mapOf<String, Any>(
            "tabOrder" to listOf("Folder", "Artist", "Album", "All", "Genre"),
            "visibleTabs" to listOf("Folder", "Artist")
        )
        coEvery { preferencesRepository.loadSettings() } returns savedSettings

        val viewModel = SettingsViewModel(preferencesRepository, musicRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf("Folder", "Artist", "Album", "All", "Genre"), state.tabOrder)
        assertEquals(listOf("Folder", "Artist"), state.visibleTabs)
    }

    @Test
    fun reorderTab_updatesTabOrderAndPersists() = runTest(dispatcher) {
        coEvery { preferencesRepository.loadSettings() } returns emptyMap()

        val viewModel = SettingsViewModel(preferencesRepository, musicRepository)
        advanceUntilIdle()

        // Move "Folder" (index 4) to index 0
        viewModel.reorderTab(4, 0)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf("Folder", "All", "Album", "Artist", "Genre"), state.tabOrder)
        coVerify { preferencesRepository.saveSettings(match { it["tabOrder"] == state.tabOrder }) }
    }

    @Test
    fun toggleTabVisibility_hidesTabAndPreservesAtLeastOneTab() = runTest(dispatcher) {
        coEvery { preferencesRepository.loadSettings() } returns emptyMap()

        val viewModel = SettingsViewModel(preferencesRepository, musicRepository)
        advanceUntilIdle()

        viewModel.toggleTabVisibility("Album")
        advanceUntilIdle()
        assertEquals(listOf("All", "Artist", "Genre", "Folder"), viewModel.uiState.value.visibleTabs)

        viewModel.toggleTabVisibility("Artist")
        viewModel.toggleTabVisibility("Genre")
        viewModel.toggleTabVisibility("Folder")
        advanceUntilIdle()
        assertEquals(listOf("All"), viewModel.uiState.value.visibleTabs)

        // Attempting to hide the last tab should not remove it
        viewModel.toggleTabVisibility("All")
        advanceUntilIdle()
        assertEquals(listOf("All"), viewModel.uiState.value.visibleTabs)
    }

    @Test
    fun moveTabLeftAndRight_updatesActiveTabOrder() = runTest(dispatcher) {
        coEvery { preferencesRepository.loadSettings() } returns emptyMap()

        val viewModel = SettingsViewModel(preferencesRepository, musicRepository)
        advanceUntilIdle()

        // Default order: [All, Album, Artist, Genre, Folder]
        // Move "Artist" left (before Album)
        viewModel.moveTabLeft("Artist")
        advanceUntilIdle()

        assertEquals(listOf("All", "Artist", "Album", "Genre", "Folder"), viewModel.uiState.value.tabOrder)

        // Move "Artist" right (after Album)
        viewModel.moveTabRight("Artist")
        advanceUntilIdle()

        assertEquals(listOf("All", "Album", "Artist", "Genre", "Folder"), viewModel.uiState.value.tabOrder)
    }
}
