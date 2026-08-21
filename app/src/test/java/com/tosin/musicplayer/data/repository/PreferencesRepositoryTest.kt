package com.tosin.musicplayer.data.repository

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PreferencesRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var context: Context
    private lateinit var repository: PreferencesRepository

    @Before
    fun setUp() {
        context = mockk()
        every { context.filesDir } returns tempFolder.root
        repository = PreferencesRepository(context)
    }

    @Test
    fun saveAndLoadSettings_preservesTabOrderAndVisibleTabsAsList() = runTest {
        val tabOrder = listOf("Folder", "All", "Album", "Artist", "Genre")
        val visibleTabs = listOf("Folder", "All", "Artist")
        val excludedFolders = listOf("/storage/emulated/0/Music/Podcast", "/storage/emulated/0/Ringtones")

        val settingsToSave = mapOf(
            "isDarkMode" to true,
            "tabOrder" to tabOrder,
            "visibleTabs" to visibleTabs,
            "excludedFolders" to excludedFolders
        )

        repository.saveSettings(settingsToSave)

        val loaded = repository.loadSettings()
        assertEquals(true, loaded["isDarkMode"])
        assertEquals(tabOrder, loaded["tabOrder"])
        assertEquals(visibleTabs, loaded["visibleTabs"])
        assertEquals(excludedFolders, loaded["excludedFolders"])
    }

    @Test
    fun saveNavigationState_doesNotCorruptTabLists() = runTest {
        val tabOrder = listOf("Folder", "Artist", "Album", "All", "Genre")
        val visibleTabs = listOf("Folder", "Artist")
        repository.saveSettings(
            mapOf(
                "tabOrder" to tabOrder,
                "visibleTabs" to visibleTabs
            )
        )

        repository.saveNavigationState("home", "Artist")

        val loaded = repository.loadSettings()
        assertEquals(tabOrder, loaded["tabOrder"])
        assertEquals(visibleTabs, loaded["visibleTabs"])
        assertEquals("home", loaded["lastClosedRoute"])
        assertEquals("Artist", loaded["lastClosedLibraryTab"])
    }
}
